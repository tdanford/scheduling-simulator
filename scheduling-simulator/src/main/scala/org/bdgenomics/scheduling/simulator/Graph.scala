package org.bdgenomics.scheduling.simulator

import collection.mutable

class Graph[T] {
  val roots : mutable.ListBuffer[GraphNode[T]] = mutable.ListBuffer()

  def insert(value: T): GraphNode[T] = {
    val node = new GraphNode[T](value)
    roots += node
    node
  }

  def connect(node1: GraphNode[T], node2: GraphNode[T]) = {
    node1.pointTo(node2)
    if (node2.isRootNode) {
      roots -= node2
    }
  }
}

class GraphNode[T](val value: T) {
  private val incomingNodes: mutable.ListBuffer[GraphNode[T]] = mutable.ListBuffer()
  private val outgoingNodes: mutable.ListBuffer[GraphNode[T]] = mutable.ListBuffer()

  def isRootNode: Boolean = incomingNodes.isEmpty

  private def remove(node: GraphNode[T]) = {
    incomingNodes -= node
  }

  def remove() = {
    outgoingNodes.foreach(_.remove(this))
  }

  def pointTo(node: GraphNode[T]) = {
    node.incomingNodes += this
    outgoingNodes += node
  }
}
