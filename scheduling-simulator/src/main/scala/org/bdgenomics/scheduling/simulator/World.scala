package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events.{ResourceDead, EventManager}
import java.util.Random
import scala.collection.mutable

class World(dag: TaskDAG, seed: Long, params: Params, schedulerFactory: SchedulerFactory) {
  val event = new EventManager
  val random = new Random(seed)
  val resourcesOutstanding = new mutable.HashSet[Resource]()

  val provisioner = new ProvisionerImpl(this, params)
  val scheduler = schedulerFactory.factory(provisioner, dag)
  var totalCost = 0
  scheduler.start()

  // figure out the next timestep we are walking toward
  do {
    event.head match {
      case (now, time, e) => {
        val stepTime = time - now
        val stepCost = resourcesOutstanding.map(_.component.cost * stepTime).reduce(_ + _)
        totalCost += stepCost
        e.execute(scheduler, provisioner)
      }
    }
  } while (!event.isEmpty)

  def createResource(component: Component): Resource = {
    val timeToDie = (random.nextInt() * component.reliability).toLong
    val resource = new ResourceImpl(this, component)
    resourcesOutstanding.add(resource)
    event.sendIn(timeToDie)
      .message(new ResourceDead(resource))
    resource
  }

  def shouldFail(task: Task): Boolean = random.nextDouble() < task.failureRate
}
