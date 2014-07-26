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

class GraphScheduler(val provider : Provider,
                     val component : Component,
                     val tasks : TaskGraph,
                     val resourceTracker : Tracker[Resource],
                     val jobTracker : Tracker[Job]) extends StatefulScheduler {

  override def blankScheduler: StatefulScheduler =
    new GraphScheduler(provider, component, tasks, Tracker[Resource](), Tracker[Job]())

  override def updateState(e: Event): StatefulScheduler = e match {
    case _ => this
  }

  override def findNextEvent(history: EventHistory, params: Parameters): Option[Event] = {

    None
  }
}
