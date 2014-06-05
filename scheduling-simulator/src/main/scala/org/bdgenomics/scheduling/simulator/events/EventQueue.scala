package org.bdgenomics.scheduling.simulator.events

import scala.collection.mutable
import scala.annotation.tailrec

class EventQueue {
  private val sentEvents: mutable.ListBuffer[(Long, Event)] = mutable.ListBuffer()
  private val eventSenders: mutable.ListBuffer[(Long, EventSender)] = mutable.ListBuffer()

  @tailrec final def dequeue: (Long, Event) = {
    val event = eventSenders.zipWithIndex.maxBy(_._1._1)
    eventSenders.remove(event._2)
    event._1._2.getMessage match {
      case None => dequeue
      case Some(e) =>
        sentEvents += ((event._1._1, e))
        (event._1._1, e)
    }
  }

  def enqueue(time: Long, eventSender: EventSender) = eventSenders += ((time, eventSender))
  def between(start: Long, end: Long): Seq[Event] = sentEvents.filter(e => e._1 >= start && e._1 <= end).map(_._2)
  def isEmpty = eventSenders.isEmpty
}
