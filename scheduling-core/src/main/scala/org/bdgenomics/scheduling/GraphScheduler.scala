/*
 * Copyright 2014 Timothy Danford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.scheduling

case class TEdge(from : Task, to: Task) extends Edge[Task] {}

trait TaskGraph extends DirectedGraph[Task,TEdge] {}

/**
 * A 'stateful scheduler' is a Scheduler with state which needs to be updated/created
 * from the event stream, before it can be run (i.e. before sampleNextEvent can be
 * invoked).
 */
trait StatefulScheduler extends Scheduler with Stateful {

  def blankScheduler : StatefulScheduler
  def updateState(e : Event) : StatefulScheduler
  def findNextEvent(history : EventHistory, params : Parameters) : Option[Event]

  override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = {

    def updater(sim : StatefulScheduler, evt : Event) : StatefulScheduler =
      sim.updateState(evt)

    val updated : StatefulScheduler =
      history.fold[StatefulScheduler](blankScheduler)(updater)

    updated.findNextEvent(history, params)
  }
}

/**
 * GraphScheduler takes a task graph, showing the dependencies between individual
 * tasks, and executes them in a topologically-ordered way using a single component
 * from a single provider.
 *
 * @param provider The provider to use
 * @param component The componnent to allocate in this provider
 * @param tasks The task graph, encoding dependencies between tasks
 * @param resourceTracker The scheduler state indicating the history of each resource available.
 * @param jobTracker The scheduler state indicating the history of each task and job
 */
class GraphScheduler(val provider : Provider,
                     val component : Component,
                     val tasks : TaskGraph,
                     val resourceTracker : Tracker[Resource],
                     val jobTracker : Tracker[Job]) extends StatefulScheduler {

  override def blankScheduler: StatefulScheduler =
    new GraphScheduler(provider, component, tasks, Tracker[Resource](), Tracker[Job]())

  override def updateState(e: Event): StatefulScheduler = e match {

    case _ =>
      new GraphScheduler( provider, component, tasks,
        resourceTracker.updateState(e),
        jobTracker.updateState(e) )
  }

  override def findNextEvent(history: EventHistory, params: Parameters): Option[Event] = {

    val topo : Seq[Task] = GraphAlgorithms.topologicalSort(tasks)

    val unfinished : Seq[Task] = topo.filter

    None
  }
}
