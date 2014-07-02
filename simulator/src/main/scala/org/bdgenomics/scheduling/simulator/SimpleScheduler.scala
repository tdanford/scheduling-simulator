package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events._

import scala.collection.mutable

class SimpleScheduler(provisioner: Provisioner, params: Params, dag: TaskDAG) extends Scheduler {
  val scheduled = new mutable.HashSet[Task]()
  val scheduledTo = new mutable.HashMap[Resource, Task]()
  override def start(): Unit = {
    scheduleLiveTasks()
  }

  private def scheduleLiveTasks(): Unit = {
    dag.getLiveTasks.map {
      case (task: Task) =>
        if (!scheduled.contains(task))
          provisioner.requestResource(params.components.head)
    }
  }

  def processEvent( e : Event ) {
    e match {
      case JobFailed(job) => jobFailed(job)
      case JobFinished(job) => jobFinished(job)
      case ResourceAvailable(rec) => resourceAvailable(rec)
      case ResourceDead(rec) => resourceDead(rec)
      case ResourceShutdown(rec) =>
    }
  }

  def resourceAvailable(resource: Resource): Unit = {
    println("running resource available")
    dag.getLiveTasks.headOption match {
      case Some(task) =>
        dag.setTaskScheduled(task, isScheduled = true)
        scheduledTo(resource) = task
        scheduled += task
        resource.execute(task)
      case None =>
        provisioner.killResource(resource)
    }
  }

  def resourceDead(resource: Resource): Unit = {
    scheduledTo.remove(resource) match {
      case Some(task) =>
        scheduled -= task
        dag.setTaskScheduled(task, isScheduled = false)
        provisioner.requestResource(resource.component)
        scheduleLiveTasks()
      case _ =>
    }
  }

  def jobFailed(job: Job): Unit = {
    provisioner.killResource(job.resource)
  }

  def jobFinished(job: Job): Unit = {
    dag.setTaskFinished(job.task)
    scheduledTo.remove(job.resource)
    scheduled -= job.task
    provisioner.killResource(job.resource)
    scheduleLiveTasks()
  }
}
