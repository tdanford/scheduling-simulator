package org.bdgenomics.scheduling.simulator.events

class EventManager {
  private var nowValue: Long = 0L
  val eventQueue: EventQueue = new EventQueue()
  def now: Long = nowValue

  def headOption: Option[(Long, Long, Event)] = {
    eventQueue.dequeue match {
      case None => None
      case Some((time, event)) =>
        val prevNow = now
        nowValue = time
        Some((prevNow, time, event))
    }
  }

  def sendIn(timeStep: Long) : EventSender =
    new EventSender(now, timeStep + now, eventQueue)
}
