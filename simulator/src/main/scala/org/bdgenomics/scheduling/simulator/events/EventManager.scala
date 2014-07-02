package org.bdgenomics.scheduling.simulator.events

class EventManager(private var nowValue : Long = 0L, val eventQueue : EventQueue = new EventQueue()) {

  def copy() : EventManager = new EventManager(nowValue, eventQueue.copy())

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

  def sendIn(timeStep: Long) : EventSender = {
    assert(timeStep >= 0, "Cannot send an event in the past")
    new EventSender(now, timeStep + now, eventQueue)
  }
}
