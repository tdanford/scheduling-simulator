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

trait Provisioner {
  def requestResource( component : Component ) : Option[Resource]
  def killResource( resource : Resource ) : Boolean
}

trait ProvisionerRequest extends Event {}

case class ResourceRequest( source : String, time : Long, component : Component ) extends Event {

}

case class KillRequest( source : String, time : Long, resource : Resource ) extends Event {

}

case class ResourceResponse( source : String, time : Long, request : ResourceRequest, resource : Option[Resource]) extends Event {

}

case class KillResponse(source : String, time : Long, request : KillRequest, result : Boolean) extends Event {

}

class ProvisionerActor( name : String, provisioner : Provisioner ) extends Actor {

  def receive = {
    case ResourceRequest(source, time, component) =>
      sender ! ResourceResponse(name, time+component.timeToStart, ResourceRequest(source, time, component), provisioner.requestResource(component))
    case KillRequest(source, time, resource) =>
      sender ! KillResponse(name, time+1, KillRequest(source, time, resource), provisioner.killResource(resource))
  }
}
