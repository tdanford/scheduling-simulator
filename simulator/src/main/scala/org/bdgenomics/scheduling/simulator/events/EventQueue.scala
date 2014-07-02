package org.bdgenomics.scheduling.simulator.events

import scala.collection.mutable
import scala.annotation.tailrec

class EventQueue(private val sentEvents : mutable.Buffer[(Long,Event)] = new mutable.ListBuffer(),
                  private val eventSenders : mutable.Buffer[(Long,EventSender)] = new mutable.ListBuffer()) {

  def copy() : EventQueue = new EventQueue(sentEvents.toBuffer, eventSenders.toBuffer)

  @tailrec final def dequeue: Option[(Long, Event)] = {
    if (eventSenders.isEmpty) None
    else {
      val event = eventSenders.zipWithIndex.minBy(_._1._1)
      eventSenders.remove(event._2)
      event._1._2.getMessage match {
        case None => dequeue
        case Some(e) =>
          sentEvents += ((event._1._1, e))
          Some((event._1._1, e))
      }
    }
  }

  def enqueue(time: Long, eventSender: EventSender) = eventSenders += ((time, eventSender))
  def between(start: Long, end: Long): Seq[Event] = sentEvents.filter(e => e._1 >= start && e._1 <= end).map(_._2)
}
