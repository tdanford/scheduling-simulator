package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events._

class JobImpl(val task: Task, val resource: Resource, world: World) extends Job {
  override def start(): Unit =
    world
      .event
      .sendIn(task.size)
      // Make sure that no resource failure has come before
      .notAfter[ResourceDead](rf => rf.resource == resource)
      .message(if (world.shouldFail(task)) JobFailed(this) else JobFinished(this))
}
