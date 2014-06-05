package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Provisioner, Scheduler, Resource}

case class ResourceAvailable(resource: Resource) extends Event {
  override def execute(s: Scheduler, p: Provisioner): Unit = s.resourceAvailable(resource)
}
