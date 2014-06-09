package org.bdgenomics.scheduling.simulator.events

import org.scalatest.FunSuite
import org.bdgenomics.scheduling.simulator.{Provisioner, Scheduler}

class EventSenderSuite extends FunSuite {
  test("Event is queued") {
    val queue = new EventQueue()
    val event = new TestEvent()
    new EventSender(0, 10, queue)
      .message(event)
    val (time, message) = queue.dequeue.get
    assert(time === 10)
    assert(message === event)
  }

  test("Event is suppressed if notAfter returns true") {
    val queue = new EventQueue()
    val goodEvent = new TestEvent()
    val supressedEvent = new TestEvent()
    new EventSender(0, 10, queue)
      .notAfter[TestEvent](_ == goodEvent)
      .message(supressedEvent)
    new EventSender(0, 5, queue)
      .message(goodEvent)
    queue.dequeue match {
      case Some((time1, message1)) =>
        assert(time1 === 5)
        assert(message1 === goodEvent)
      case None => fail("Should be able to dequeue at least one value")
    }

    queue.dequeue match {
      case Some(_) => fail("Should not be able to dequeue a value after already dequeueing")
      case None => {}
    }
  }
}
