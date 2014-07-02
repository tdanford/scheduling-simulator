package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Scheduler, Resource}

case class ResourceDead(resource: Resource) extends Event {
}
