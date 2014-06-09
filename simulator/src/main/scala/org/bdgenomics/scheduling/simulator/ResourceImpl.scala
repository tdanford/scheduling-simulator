package org.bdgenomics.scheduling.simulator

class ResourceImpl(world: World, val component: Component) extends Resource {
  override def shutdown(): Unit =
    world.resourceShutdown(this)

  override def execute(t: Task): Job =
    world.createJob(this, t)

  override def toString: String =
    component.toString
}
