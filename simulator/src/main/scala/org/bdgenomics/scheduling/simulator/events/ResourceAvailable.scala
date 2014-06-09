package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Scheduler, Resource}

case class ResourceAvailable(resource: Resource) extends Event {
  override def execute(s: Scheduler): Unit = s.resourceAvailable(resource)
}
