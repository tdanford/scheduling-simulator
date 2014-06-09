package org.bdgenomics.scheduling.simulator

import org.scalatest.FunSuite

class SimpleWorldSuite extends FunSuite {
  val simpleSchedulerFactory = new SchedulerFactory {
    override def factory(provisioner: Provisioner, params: Params, dag: TaskDAG): Scheduler =
      new SimpleScheduler(provisioner, params, dag)
  }

  val taskGraph = new Graph[Task]()
  taskGraph.insert(new Task(10, 0.0))
  val dag = new TaskDAG(taskGraph)

  val params = new Params(Seq(new Component("simple", 1.0, 1.0, 10L)))

  test("Run Simple World") {
    val world = new World(dag, 5L, params, simpleSchedulerFactory)
    assert(world.totalCost === 20.0)
    assert(world.totalTime === 20L)
  }
}
