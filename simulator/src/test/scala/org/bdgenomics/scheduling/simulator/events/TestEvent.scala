package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.{Provisioner, Scheduler}

class TestEvent extends Event {
  override def execute(s: Scheduler, p: Provisioner): Unit = {}
}
