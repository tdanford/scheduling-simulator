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

import java.util.UUID

/**
 * This is a little tricky -- the Provider isn't an EventSource in its own right,
 * it's a component to another EventSource (in this case, the Scheduler).  Therefore,
 * it provides routines for constructing Events corresponding to the user's decisions,
 * but it doesn't live as a first-class EventSource on its own.
 */
trait Provider {

  def createResource( history : EventHistory, params : Parameters, component : Component ) : Event
  def shutdownResource( history : EventHistory, params : Parameters, resource : Resource ) : Event
}

class CloudProvider extends Provider {

  def createId() : String = UUID.randomUUID().toString

  override def createResource(history: EventHistory, params: Parameters, component: Component): Event = {
    val startupTime = history.currentTime + params.sampleResourceStartupTime()
    StartupEvent(startupTime, Resource(createId(), component, startupTime))
  }

  override def shutdownResource(history: EventHistory, params: Parameters, resource: Resource): Event =
    ShutdownEvent(history.currentTime, resource)
}

