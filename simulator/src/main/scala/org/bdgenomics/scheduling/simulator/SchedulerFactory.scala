package org.bdgenomics.scheduling.simulator

trait SchedulerFactory {
  def factory(provisioner: Provisioner, dag: TaskDAG): Scheduler
}
