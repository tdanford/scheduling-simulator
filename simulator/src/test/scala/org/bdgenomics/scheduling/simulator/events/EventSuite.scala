package org.bdgenomics.scheduling.simulator.events

import org.scalatest.FunSuite
import org.bdgenomics.scheduling.simulator.{Resource, Job, Scheduler, ResourceImpl}

class EventSuite extends FunSuite {
  test("Events properly invoke notifications") {
    val available = new ResourceAvailable(new ResourceImpl(null, null))
    var invoked = false
    available.execute(new Scheduler {
      override def resourceAvailable(resource: Resource): Unit = invoked = true

      override def resourceDead(resource: Resource): Unit = ???

      override def jobFailed(job: Job): Unit = ???

      override def jobFinished(job: Job): Unit = ???

      override def start(): Unit = ???
    })
    assert(invoked)
  }
}
