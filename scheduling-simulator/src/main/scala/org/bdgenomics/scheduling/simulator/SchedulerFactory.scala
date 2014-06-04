package org.bdgenomics.scheduling.simulator

class SchedulerFactory {
  def factory(provisioner: Provisioner, dag: TaskDAG): Scheduler
}
