package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events._
import scala.collection.mutable
import scala.annotation.tailrec
import org.apache.commons.math3.random.RandomGenerator
import org.bdgenomics.scheduling.simulator.events.ResourceDead
import org.bdgenomics.scheduling.simulator.events.JobFailed
import scala.Some

/**
 * This is our simulator class.
 *
 * @param dag
 * @param random
 * @param params
 * @param schedulerFactory
 */
class World(dag: TaskDAG,
            private val random: Sampler,
            params: Params,
            schedulerFactory: SchedulerFactory) {

  val event = new EventManager

  // For cost calculation.
  private val resourcesOutstanding = new mutable.HashSet[Resource]()

  private val provisioner = new ProvisionerImpl(this, params)
  private val scheduler = schedulerFactory.factory(provisioner, params, dag)
  private val taskDistribution = params.taskDistribution(random.randomGenerator())
  private val taskFailureDistribution = params.taskFailureDistribution(random.randomGenerator())
  private val resourceDistribution = params.resourceDistribution(random.randomGenerator())
  var totalCost = 0.0
  def totalTime = event.now

  scheduler.start()

  processStep()

  @tailrec private def processStep() {
    event.headOption match {
      case Some((now, time, e)) =>
        if (!resourcesOutstanding.isEmpty) {
          val stepTime = time - now
          var stepCost = 0.0
          resourcesOutstanding.foreach(c => stepCost += c.component.cost * stepTime)
          totalCost += stepCost
        }
        scheduler.processEvent(e)
        processStep()
      case None =>
    }
  }

  /**
   * This is called by the provisioner
   * @param component
   * @return
   */
  def createResource(component: Component): Resource = {
    val timeToDie = (resourceDistribution.sample() * component.reliability).toLong
    val resource = new ResourceImpl(this, component)
    resourcesOutstanding.add(resource)
    event.sendIn(timeToDie)
      .notAfter(ResourceUnavailable.notAfter(resource))
      .message(new ResourceDead(resource))
    resource
  }

  /**
   * This is called by the resource
   * @param resource
   */
  def resourceShutdown(resource: Resource): Unit = {
    resourcesOutstanding.remove(resource)
  }

  /**
   * This is called by the resource
   * @param resource
   * @param task
   * @return
   */
  def createJob(resource: Resource, task: Task): Job = {
    val job = new JobImpl(task, resource, this)
    event
      .sendIn((taskDistribution.sample() * task.size).toLong)
      // Make sure that no resource failure has come before
      .notAfter(ResourceUnavailable.notAfter(resource))
      .message(if (taskFailureDistribution.sample() < task.failureRate) JobFailed(job) else JobFinished(job))
    job
  }
}
