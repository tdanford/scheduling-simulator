package org.bdgenomics.scheduling.simulator.events

class EventSender(now: Long,
                  sendAt: Long,
                  queue: EventQueue,
                  event: Option[Event] = None,
                  test: () => Boolean = () => true) {
  def getMessage: Option[Event] = if (test()) event else None

  def notAfter(filter: Event => Boolean = _ => true): EventSender =
    new EventSender(now, sendAt, queue, event, () => test()
      && queue.between(now, sendAt).forall(!filter(_)))

  def message(message: Event) = {
    println("Sending(%d): %s".format(now + sendAt, message))
    queue.enqueue(sendAt, new EventSender(now, sendAt, queue, Some(message), test))
  }
}
