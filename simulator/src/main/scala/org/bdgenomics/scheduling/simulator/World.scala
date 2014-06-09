package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events.{ResourceUnavailable, ResourceDead, EventManager}
import java.util.Random
import scala.collection.mutable
import scala.annotation.tailrec

class World(dag: TaskDAG, seed: Long, params: Params, schedulerFactory: SchedulerFactory) {
  val event = new EventManager
  private val random = new Random(seed)
  private val resourcesOutstanding = new mutable.HashSet[Resource]()

  private val provisioner = new ProvisionerImpl(this, params)
  private val scheduler = schedulerFactory.factory(provisioner, params, dag)
  var totalCost = 0.0
  def totalTime = event.now

  scheduler.start()

  processStep()

  @tailrec private def processStep() {
    event.headOption match {
      case Some((now, time, e)) =>
        if (!resourcesOutstanding.isEmpty) {
          val stepTime = time - now
          val stepCost = resourcesOutstanding.map(_.component.cost * stepTime).reduce(_ + _)
          totalCost += stepCost
        }
        e.execute(scheduler)
        processStep()
      case None =>
    }
  }

  def createResource(component: Component): Resource = {
    val timeToDie = random.nextInt(100000000).toLong
    val resource = new ResourceImpl(this, component)
    resourcesOutstanding.add(resource)
    event.sendIn(timeToDie)
      .notAfter(ResourceUnavailable.notAfter(resource))
      .message(new ResourceDead(resource))
    resource
  }

  def resourceShutdown(resource: Resource): Unit = {
    resourcesOutstanding.remove(resource)
  }

  def createJob(resource: Resource, task: Task): Job = {
    val job = new JobImpl(task, resource, this)
    job.start()
    job
  }

  def shouldFail(task: Task): Boolean = random.nextDouble() < task.failureRate
}
