package org.bdgenomics.scheduling.simulator

class JobImpl(val task: Task, val resource: Resource, world: World) extends Job {
  override def toString: String = "%s on %s".format(task, resource)
}
