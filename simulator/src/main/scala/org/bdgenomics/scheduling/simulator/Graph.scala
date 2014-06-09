package org.bdgenomics.scheduling.simulator

import collection.mutable

trait GraphLike[T] {
  type Node
  def insert(value: T): Node
  def connect(node1: Node, node2: Node)
  def remove(node: Node)
}

class Graph[T] extends GraphLike[T] {
  class GraphNode(val value: T) {
    private val incomingNodes: mutable.ListBuffer[GraphNode] = mutable.ListBuffer()
    private val outgoingNodes: mutable.ListBuffer[GraphNode] = mutable.ListBuffer()

    def isRootNode: Boolean = incomingNodes.isEmpty

    private def removeNode(node: GraphNode) = {
      incomingNodes -= node
    }

    def remove(): Seq[GraphNode] = {
      outgoingNodes.foreach(r => r.removeNode(this))
      outgoingNodes
    }

    def pointTo(node: GraphNode) = {
      node.incomingNodes += this
      outgoingNodes += node
    }
  }

  type Node = GraphNode

  val roots : mutable.ListBuffer[GraphNode] = mutable.ListBuffer()

  def insert(value: T): Node = {
    val node = new GraphNode(value)
    roots += node
    node
  }

  def connect(node1: Node, node2: Node) = {
    if (node2.isRootNode) {
      roots -= node2
    }
    node1.pointTo(node2)
  }

  def remove(node: Node) =
    if (roots.contains(node)) {
      roots -= node
      val nodes = node.remove()
      for (node <- nodes if node.isRootNode) roots += node
    } else throw new IllegalArgumentException("Can only remove a node which is a root")
}
