/**
 * Copyright 2014 Genome Bridge LLC
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
package com.tdanford.simulator

/**
 * Represents the basic elastic resource on which tasks can be executed.
 * @param nodeId A unique string identifying this node.
 */
case class Node(nodeId : String) {
}

/**
 * Represents the elastic source of nodes -- e.g. EC2 or GCE
 *
 * There are two requests we can make of an elastic node resource:
 * Either startup a node, or shut an existing node down.
 */
trait ElasticNodeResource {

  def requestNode()
  def shutdownNode(n : Node)
}
