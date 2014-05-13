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


//maybe roll this into the NodeType or pull more out of the NodeType
/**
 * Represents the instance type of a node, like a spot instance
 * @param name the name of the instance type (spot)
 * @param isSpot is this a "spot" instance, can it be destroyed randomly?  Does the price fluctuate
 */
case class InstanceType(name: String, isSpot: Boolean)

/**
 * Represents the basic instance types that can be requested
 * such as aws c3.large
 * @param name A unique name such as c3.large per vendor
 * @param mem The total memory of a node
 * @param cpu The cpu count of a node normalized to aws CPUs
 */
case class NodeType(name: String, mem: Int, cpu: Int)

/**
 * Represents the basic elastic resource on which tasks can be executed.
 * @param nodeId A unique string identifying this node.
 * @param nodeType What kind of node is this (vendor, c3.large, etc.)
 * @param instanceType what kind of instance is this (spot Instance?)
 * @param price How much per hour in dollars does this node cost?
 */
case class Node(nodeId : String, nodeType: NodeType, instanceType: InstanceType, price: Float) {


}

/**
 * Represents the elastic source of nodes -- e.g. EC2 or GCE
 *
 * There are two requests we can make of an elastic node resource:
 * Either startup a node, or shut an existing node down.
 */
trait ElasticNodeResource {
  //we can have a default type if one is not provided
  def requestNode(nodeType :Option[NodeType])
  def shutdownNode(n : Node)
}
