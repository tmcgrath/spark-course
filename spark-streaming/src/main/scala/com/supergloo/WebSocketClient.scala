package com.supergloo

import java.net.{InetSocketAddress, URI}
import java.nio.charset.Charset
import java.util.concurrent.Executors

import akka.actor.ActorRef
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocketx._
import org.jboss.netty.util.CharsetUtil

import scala.collection.JavaConversions._


/**
  * Usage of the simple websocket client:
  * <pre>
  * WebSocketClient(new URI("ws://localhost:8080/thesocket")) {
  * case Connected(client) => println("Connection has been established to: " + client.url.toASCIIString)
  * case Disconnected(client, _) => println("The websocket to " + client.url.toASCIIString + " disconnected.")
  * case TextMessage(client, message) => {
  * println("RECV: " + message)
  * client send ("ECHO: " + message)
  * }
  * }
  * </pre>
  */
object WebSocketClient {

  object Messages {

    sealed trait WebSocketClientMessage

    case object Connecting extends WebSocketClientMessage

    case class ConnectionFailed(client: WebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage

    case class Connected(client: WebSocketClient) extends WebSocketClientMessage

    case class TextMessage(client: WebSocketClient, text: String) extends WebSocketClientMessage

    case class WriteFailed(client: WebSocketClient, message: String, reason: Option[Throwable]) extends WebSocketClientMessage

    case object Disconnecting extends WebSocketClientMessage

    case class Disconnected(client: WebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage

    case class Error(client: WebSocketClient, th: Throwable) extends WebSocketClientMessage

  }

  type Handler = PartialFunction[Messages.WebSocketClientMessage, Unit]
  type FrameReader = WebSocketFrame => String

  val defaultFrameReader = (_: WebSocketFrame) match {
    case f: TextWebSocketFrame => f.getText
    case _ => throw new UnsupportedOperationException("Only single text frames are supported for now")
  }

  def apply(url: URI, version: WebSocketVersion = WebSocketVersion.V13, reader: FrameReader = defaultFrameReader)(handle: Handler): WebSocketClient = {
    require(url.getScheme.startsWith("ws"), "The scheme of the url should be 'ws' or 'wss'")
    new DefaultWebSocketClient(url, version, handle, reader)
  }

  def apply(url: URI, handle: ActorRef): WebSocketClient = {
    require(url.getScheme.startsWith("ws"), "The scheme of the url should be 'ws' or 'wss'")
    WebSocketClient(url) { case x => handle ! x }
  }

  private class WebSocketClientHandler(handshaker: WebSocketClientHandshaker, client: WebSocketClient) extends SimpleChannelUpstreamHandler {

    import Messages._

    override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      client.handler(Disconnected(client))
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      e.getMessage match {
        case resp: HttpResponse if handshaker.isHandshakeComplete =>
          throw new WebSocketException("Unexpected HttpResponse (status=" + resp.getStatus + ", content="
            + resp.getContent.toString(CharsetUtil.UTF_8) + ")")
        case resp: HttpResponse =>
          handshaker.finishHandshake(ctx.getChannel, e.getMessage.asInstanceOf[HttpResponse])
          client.handler(Connected(client))

        case f: TextWebSocketFrame => client.handler(TextMessage(client, f.getText))
        case _: PongWebSocketFrame =>
        case _: CloseWebSocketFrame => ctx.getChannel.close()
      }
    }


    override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
      client.handler(Error(client, e.getCause))
      e.getChannel.close()
    }

  }

  private class DefaultWebSocketClient(
                                        val url: URI,
                                        version: WebSocketVersion,
                                        private[this] val _handler: Handler,
                                        val reader: FrameReader = defaultFrameReader) extends WebSocketClient {
    val normalized = url.normalize()
    val tgt = if (normalized.getPath == null || normalized.getPath.trim().isEmpty) {
      new URI(normalized.getScheme, normalized.getAuthority, "/", normalized.getQuery, normalized.getFragment)
    } else normalized

    val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))
    val handshaker = new WebSocketClientHandshakerFactory().newHandshaker(tgt, version, null, false, Map.empty[String, String])
    val self = this
    var channel: Channel = _

    import Messages._

    val handler = _handler orElse defaultHandler

    private def defaultHandler: Handler = {
      case Error(_, ex) => ex.printStackTrace()
      case _: WebSocketClientMessage =>
    }


    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline()
        if (version == WebSocketVersion.V00)
          pipeline.addLast("decoder", new WebSocketHttpResponseDecoder)
        else
          pipeline.addLast("decoder", new HttpResponseDecoder)

        pipeline.addLast("encoder", new HttpRequestEncoder)
        pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker, self))
        pipeline
      }
    })

    import WebSocketClient.Messages._

    def connect = {
      if (channel == null || !channel.isConnected) {
        val listener = futureListener { future =>
          if (future.isSuccess) {
            synchronized {
              channel = future.getChannel
            }
            handshaker.handshake(channel)
          } else {
            handler(ConnectionFailed(this, Option(future.getCause)))
          }
        }
        handler(Connecting)
        val fut = bootstrap.connect(new InetSocketAddress(url.getHost, url.getPort))
        fut.addListener(listener)
        fut.await(5000L)
      }
    }

    def disconnect = {
      if (channel != null && channel.isConnected) {
        handler(Disconnecting)
        channel.write(new CloseWebSocketFrame())
      }
    }

    def send(message: String, charset: Charset = CharsetUtil.UTF_8) = {
      channel.write(new TextWebSocketFrame(ChannelBuffers.copiedBuffer(message, charset))).addListener(futureListener { fut =>
        if (!fut.isSuccess) {
          handler(WriteFailed(this, message, Option(fut.getCause)))
        }
      })
    }

    def futureListener(handleWith: ChannelFuture => Unit) = new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        handleWith(future)
      }
    }
  }

  /**
    * Fix bug in standard HttpResponseDecoder for web socket clients. When status 101 is received for Hybi00, there are 16
    * bytes of contents expected
    */
  class WebSocketHttpResponseDecoder extends HttpResponseDecoder {

    val codes = List(101, 200, 204, 205, 304)

    protected override def isContentAlwaysEmpty(msg: HttpMessage) = {
      msg match {
        case res: HttpResponse => codes contains res.getStatus.getCode
        case _ => false
      }
    }
  }

  /**
    * A WebSocket related exception
    *
    * Copied from https://github.com/cgbystrom/netty-tools
    */
  class WebSocketException(s: String, th: Throwable) extends java.io.IOException(s, th) {
    def this(s: String) = this(s, null)
  }

}

trait WebSocketClient {

  def url: URI

  def reader: WebSocketClient.FrameReader

  def handler: WebSocketClient.Handler

  def connect

  def disconnect

  def send(message: String, charset: Charset = CharsetUtil.UTF_8)
}
