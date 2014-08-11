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
 * A Component is a Resource-template.  It encodes the cost and characteristics of a Resource
 * that would be created by a particular Provider, and is used as an argument to the Provider in
 * order to create a Resource.
 *
 * @param name A unique ID for the Component
 * @param cost The cost-per-unit-time of the Resource while it's running
 */
case class Component(name : String, cost : Double) {
}

/**
 * A Resource is a running Component, an element on which Jobs can be started and executed.
 * Resources start, fail (randomly), and are shutdown.
 *
 * @param id The unique identifier for this Resource
 * @param component The component that was used to create this resource
 * @param startTime The time at which this resource was started
 */
case class Resource(id : String, component : Component, startTime : Long )
  extends EventSource {

  override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] =
    Some(FailureEvent(history.currentTime + params.sampleResourceFailureTime(), this))
}

case class StartupEvent(time : Long, resource : Resource) extends InitialEvent[Resource](time, resource) {
}

case class FailureEvent(time : Long, resource : Resource) extends TerminalEvent[Resource](time, resource) {
}

case class ShutdownEvent(time : Long, resource : Resource) extends TerminalEvent[Resource](time, resource) {
}
