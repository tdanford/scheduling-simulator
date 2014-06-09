package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.Scheduler

trait Event {
  def execute(s: Scheduler)
}
