package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Scheduler, Job}

case class JobFinished(job: Job) extends Event {
}
