package org.apache.spark.streaming

import org.apache.spark.util.ManualClock

/**
  * This class is defined in this package as the ManualClock is
  * private in the "spark" package
  *
  * @param ssc streaming context
  */
class ClockWrapper(ssc: StreamingContext) {

  def getTimeMillis(): Long = manualClock().getTimeMillis()

  def setTime(timeToSet: Long) = manualClock().setTime(timeToSet)

  def advance(timeToAdd: Long) = manualClock().advance(timeToAdd)

  def waitTillTime(targetTime: Long): Long = manualClock().waitTillTime(targetTime)

  private def manualClock(): ManualClock = {
    ssc.scheduler.clock.asInstanceOf[ManualClock]
  }
}
