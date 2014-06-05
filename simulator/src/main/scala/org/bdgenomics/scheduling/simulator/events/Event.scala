package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Provisioner, Scheduler}

trait Event {
  def execute(s: Scheduler, p: Provisioner)
}
