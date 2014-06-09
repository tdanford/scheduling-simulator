package org.bdgenomics.scheduling.simulator

import org.scalatest.FunSuite

class GraphSuite extends FunSuite {
  test("Graph identifies roots") {
    val root = 1
    val graph = new Graph[Int]()
    graph.insert(root)
    assert(graph.roots.size === 1)
    assert(graph.roots.exists(_.value === root))
  }

  test("Graph does not include all nodes as roots") {
    val root = 1
    val next = 2
    val graph = new Graph[Int]()
    val rootNode = graph.insert(root)
    val nextNode = graph.insert(next)
    graph.connect(rootNode, nextNode)
    assert(graph.roots.size === 1)
    assert(graph.roots.forall(_.value !== next))
  }

  test("Graph can properly remove root nodes and new nodes become root") {
    val first = 1
    val second = 2
    val third = 3
    val graph = new Graph[Int]()
    val firstNode = graph.insert(first)
    val secondNode = graph.insert(second)
    val thirdNode = graph.insert(third)
    graph.connect(firstNode, secondNode)
    graph.connect(secondNode, thirdNode)
    assert(graph.roots.size === 1)
    assert(graph.roots.forall(_.value === first))
    graph.remove(firstNode)
    assert(graph.roots.size === 1)
    assert(graph.roots.forall(_.value === second))
  }

  test("Graph can remove all nodes and become empty") {
    val first = 1
    val second = 2
    val graph = new Graph[Int]()
    val firstNode = graph.insert(first)
    val secondNode = graph.insert(second)
    graph.connect(firstNode, secondNode)
    assert(graph.roots.size === 1)
    assert(graph.roots.forall(_.value === first))
    graph.remove(firstNode)
    assert(graph.roots.size === 1)
    assert(graph.roots.forall(_.value === second))
    graph.remove(secondNode)
    assert(graph.roots.isEmpty)
  }
}
