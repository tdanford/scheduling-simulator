/**
 * Copyright 2014 Timothy Danford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.scheduling.simulator

import akka.actor.Actor

import scala.collection.mutable

class Simulator( params : SimulatorParams, history : Seq[Event], future : Seq[Event] ) {

  val pending = new mutable.PriorityQueue[Event]() ++ future

  def ++( futureEvents : Seq[Event] ) : Simulator =
    new Simulator(params, history, future++futureEvents)

  def +( event : Event ) : Simulator = ++(Seq(event))

  def findNextEvent() : Event =
    future.sortBy(_.time).head

  def processEvent( event : Event ) : Simulator = {

  }
}

class SimulatorActor( sim : Simulator ) extends Actor {

  var currentSimulator = sim

  def receive = {
    case x : Event => currentSimulator += x
  }
}
