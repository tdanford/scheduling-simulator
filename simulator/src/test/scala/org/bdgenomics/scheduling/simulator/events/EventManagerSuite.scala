package org.bdgenomics.scheduling.simulator.events

import org.scalatest.FunSuite

class EventManagerSuite extends FunSuite {
  test("EventManager can return a message") {
    val em = new EventManager()
    em.sendIn(5).message(new TestEvent())
    em.headOption match {
      case Some((0, 5, _)) => {}
      case _ => fail("Expected a specific message back")
    }
    assert(em.now === 5, "Time must progress")
  }
}
