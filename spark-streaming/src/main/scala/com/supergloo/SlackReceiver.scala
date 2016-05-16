package com.supergloo
 
import java.net.URI

// import com.supergloo.WebSocketClient.Messages.TextMessage 
import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.jfarcand.wcs.{TextListener, WebSocket}
 
import scala.util.parsing.json.JSON
import scalaj.http.Http
 
/**
* Spark Streaming Example Slack Receiver from Slack
*/
class SlackReceiver(token: String) extends Receiver[String](StorageLevel.MEMORY_ONLY) 
                                   with Runnable with Logging {
 
  private val slackUrl = "https://slack.com/api/rtm.start"
 
  @transient
  private var thread: Thread = _
 
  override def onStart(): Unit = {
     thread = new Thread(this)
     thread.start()
  }
 
  override def onStop(): Unit = {
     thread.interrupt()
  }
 
  override def run(): Unit = {
     receive()
   }
 
  private def receive(): Unit = {
     val webSocket = WebSocket().open(webSocketUrl())
     webSocket.listener(new TextListener {
       override def onMessage(message: String) {
         store(message)
       }
     })
  }
 
  private def webSocketUrl(): String = {
    val response = Http(slackUrl).param("token", token).asString.body
    JSON.parseFull(response).get.asInstanceOf[Map[String, Any]].get("url").get.toString
  }
 
  //  private def receive(): Unit = {
  //   val webSocket = WebSocketClient(new URI(webSocketUrl())){
  //     case TextMessage(client, message) => {
  //       store(message)
  //     }
  //   }
  // }

  // private def webSocketUrl(): String = {
  //   val response = Http(slackUrl).param("token", token).asString.body
  //   JSON.parseFull(response).get.asInstanceOf[Map[String, Any]].get("url").get.toString
  // }
}