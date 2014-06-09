package org.bdgenomics.scheduling.simulator

trait SchedulerFactory {
  def factory(provisioner: Provisioner, params: Params, dag: TaskDAG): Scheduler
}
