package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.Scheduler

trait Event {
  def source : String
  def execute(s: Scheduler)
}
