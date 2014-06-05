package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Provisioner, Scheduler, Job}

case class JobFinished(job: Job) extends Event {
  override def execute(s: Scheduler, p: Provisioner): Unit = s.jobFinished(job)
}
