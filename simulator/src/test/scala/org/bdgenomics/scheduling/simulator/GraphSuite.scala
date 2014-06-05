package org.bdgenomics.scheduling.simulator

import org.scalatest.FunSuite

class GraphSuite extends FunSuite {
  test("Graph identifies roots") {
    val root = 1
    val graph = new Graph[Int]()
    graph.insert(root)
    graph.roots.exists(_.value === root)
  }

  test("Graph does not include all nodes as roots") {
    val root = 1
    val next = 2
    val graph = new Graph[Int]()
    val rootNode = graph.insert(root)
    val nextNode = graph.insert(next)
    graph.connect(rootNode, nextNode)
    graph.roots.forall(_.value !== next)
  }
}
