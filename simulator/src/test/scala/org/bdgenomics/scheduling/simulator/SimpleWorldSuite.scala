package org.bdgenomics.scheduling.simulator

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.commons.math3.random.{ISAACRandom, RandomGenerator}
import org.apache.commons.math3.distribution.{ParetoDistribution, RealDistribution}

class SimpleWorldSuite extends FunSuite with BeforeAndAfter {
  var simpleSchedulerFactory: SchedulerFactory = null
  var params: Params = null
  before {
    simpleSchedulerFactory = new SchedulerFactory {
      override def factory(provisioner: Provisioner, params: Params, dag: TaskDAG): Scheduler =
        new SimpleScheduler(provisioner, params, dag)
    }

    params = new Params {
      override def components: Seq[Component] = Seq(new Component("simple", 1.0, 1.0, 10L))

      override def taskDistribution(random: RandomGenerator): RealDistribution = new ParetoDistribution(random, 1.0, 1.0)

      override def taskFailureDistribution(random: RandomGenerator): RealDistribution = new ParetoDistribution(random, 1000.0, 1.0)

      override def resourceDistribution(random: RandomGenerator): RealDistribution = new ParetoDistribution(random, 1000.0, 1.0)
    }
  }

  test("Run Simple World") {
    val taskGraph = new Graph[Task]()
    taskGraph.insert(new Task(10, 0.0))
    val dag = new TaskDAG(taskGraph)
    val world = new World(dag, new RGSampler(Some(100L)), params, simpleSchedulerFactory)
    assert(world.totalCost === 20.0)
    assert(world.totalTime === 20L)
  }

  test("Some sort of success") {
    val taskGraph = new Graph[Task]()
    taskGraph.insert(new Task(1500, 0.0))
    val dag = new TaskDAG(taskGraph)
    val world = new World(dag, new RGSampler(Some(100L)), params, simpleSchedulerFactory)
    assert(world.totalCost === 26771.0)
    assert(world.totalTime === 8410L)
  }

  test("Can run tasks in parallel") {
    val taskGraph = new Graph[Task]()
    taskGraph.insert(new Task(100, 0.0))
    taskGraph.insert(new Task(250, 0.0))
    val dag = new TaskDAG(taskGraph)
    val world = new World(dag, new RGSampler(Some(100L)), params, simpleSchedulerFactory)
    assert(world.totalCost === 653.0)
    assert(world.totalTime === 391L)
  }
}
