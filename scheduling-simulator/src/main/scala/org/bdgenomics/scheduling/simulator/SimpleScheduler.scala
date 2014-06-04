package org.bdgenomics.scheduling.simulator

import scala.collection.mutable

class SimpleScheduler(provisioner: Provisioner, params: Params, dag: TaskDAG) extends Scheduler {
  val scheduledTo = new mutable.HashMap[Resource, Task]()
  override def start(): Unit = {
    scheduleLiveTasks()
  }

  private def scheduleLiveTasks(): Unit = {
    dag.getLiveTasks.map {
      case (task: Task) => {
        provisioner.requestResource(params.getComponents.head)
      }
    }
  }

  override def resourceAvailable(resource: Resource): Unit = {
    val task = dag.getLiveTasks.head
    dag.setTaskScheduled(task, true)
    scheduledTo(resource) = task
    resource.execute(task)
  }

  override def resourceDead(resource: Resource): Unit = {
    scheduledTo.remove(resource) match {
      case Some(task) => {
        dag.setTaskScheduled(task, false)
        provisioner.requestResource(resource.component)
      }
      case None => {}
    }
  }

  override def jobFailed(job: Job): Unit = {
    provisioner.killResource(job.resource)
  }

  override def jobFinished(job: Job): Unit = {
    dag.setTaskFinished(job.task)
    scheduledTo.remove(job.resource)
    provisioner.killResource(job.resource)
    scheduleLiveTasks()
  }
}
