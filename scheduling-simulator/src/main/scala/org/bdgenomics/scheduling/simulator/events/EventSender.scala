package org.bdgenomics.scheduling.simulator.events

class EventSender(now: Long,
                  sendAt: Long,
                  queue: EventQueue,
                  event: Option[Event] = None,
                  test: () => Boolean = () => true) {
  def getMessage: Option[Event] = if (test()) event else None

  def notAfter[T <: Event](filter: T => Boolean = _ => true): EventSender =
  new EventSender(now, sendAt, queue, event, () => test()
    && queue.between(now, sendAt).forall(e => !e.isInstanceOf[T] || !filter(e.asInstanceOf[T])))

  def message(message: Event) = queue.enqueue(sendAt, new EventSender(now, sendAt, queue, Some(message), test))
}
