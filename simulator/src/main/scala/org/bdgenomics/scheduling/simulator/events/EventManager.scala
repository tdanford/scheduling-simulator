package org.bdgenomics.scheduling.simulator.events

class EventManager {
  private var nowValue: Long = 0L
  val eventQueue: EventQueue = new EventQueue()
  def now: Long = nowValue

  def head: (Long, Long, Event) = {
    val (time, event) = eventQueue.dequeue
    val ret = (now, time, event)
    nowValue = time
    ret
  }

  def sendIn(timeStep: Long) : EventSender =
    new EventSender(now, timeStep + now, eventQueue)

  def isEmpty = eventQueue.isEmpty
}
