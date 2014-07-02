package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Scheduler, Job}

case class JobFailed(job: Job) extends Event {
}
