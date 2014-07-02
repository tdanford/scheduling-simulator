package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events.Event

trait Scheduler {
  def start()
  def processEvent( e : Event )
}
