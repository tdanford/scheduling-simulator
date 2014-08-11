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

/**
 * FulfillEvent executes a request and, by doing so, removes the corresponding Request (EventSource)
 * from the Simulators event source list.
 *
 * @param time
 * @param source
 */
case class FulfillEvent(time : Long, source : Request) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    source.action(sim).map(s => s.update( srcs => srcs.filter(src => src != source) ))
}

/**
 * RequestEvent puts an Request (a one-time EventSource) in to the EventSource list of the simulator.
 *
 * The order here is three-fold.  If a Scheduler (or other actor) wants to make a request, which
 * may take time to satisfy but _which is wishes to remember_ (for its own stateful purposes), it
 * needs to:
 * 1. return a RequestEvent from its own sampleNextEvent method
 * 2. the RequestEvent, when chosen for simulation, puts the Request _into the list of event sources_.
 *    At this point, the RequestEvent is a marker which the Scheduler can use to update its own state
 *    from the EventHistory, in the future.
 * 3. Finally, the Request proposes a single FulfillEvent.  When the FulfillEvent is accepted, the
 *    Request's action is performed and the Request is removed from the list of EventSources.
 *
 * @param time
 * @param source
 */
case class RequestEvent( time : Long, source : Request ) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(sim.update(source +: _))
}

abstract class Request extends EventSource {

  def time : Long
  def requester : Any
  def action : Simulator=>Option[Simulator]

  def sampleNextEvent( history : EventHistory, params : Parameters ) : Option[Event] =
    Some(FulfillEvent(time, this))
}

case class ResourceRequest(time : Long,
                           requester : Scheduler,
                           resource : Resource) extends Request {
  def action : Simulator=>Option[Simulator] = {
    case sim : Simulator =>
      Some(sim.update( sources => resource +: sources ))
  }
}

case class JobRequest(time : Long,
                      requester : Scheduler,
                      job : Job) extends Request {
  def action : Simulator=>Option[Simulator] = {
    case sim : Simulator =>
      Some(sim.update( sources => job +: sources ))
  }
}

