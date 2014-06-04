package org.bdgenomics.scheduling.simulator

class World(dag: TaskDAG, seed: Long, params: Params, schedulerFactory: SchedulerFactory) {
  val provisioner = new ProvisionerImpl(this, params)
  val scheduler = schedulerFactory.factory(provisioner, dag)
  scheduler.start()

}
