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
package org.bdgenomics.scheduling.algorithms

import java.util.Random

import org.scalatest.FunSuite

class GraphSuite extends FunSuite {

  def graph( edges : (Int,Int)* ) : DirectedGraph[NODE, DEDGE] =
    Graph[NODE, DEDGE]( edges.map( p => DEDGE(NODE(p._1), NODE(p._2)) ) )

  /**
   * Generates a random permutation on a set of numbers [0, n)
   * @param r
   * @param n
   * @return
   */
  def permutation( r : Random, n : Int ) : Seq[Int] = {
    val indices = (0 until n).toArray

    def swap(left : Int, right : Int) {
      val s = indices(left)
      indices(left) = indices(right)
      indices(right) = s
    }

    (0 until n).foreach { i =>
      swap(r.nextInt(n), r.nextInt(n))
    }
    indices
  }

  /**
   * Generates a random DAG for the given size parameters.
   *
   * @param r
   * @param nodes
   * @param edges
   * @return
   */
  def randomDAG( r : Random, nodes : Int, edges : Int ) : DirectedGraph[NODE, DEDGE] = {

    val ns = permutation(r, nodes).map(i => NODE(i))
    val nodeList = (0 until nodes).map( i => NODE(i) )

    def randomEdge(i : Int) : DEDGE = {
      // the -1/+1 bit is so that we never choose the *first* node in the ordering
      val end = r.nextInt(nodes-1) + 1
      val start = r.nextInt(end)
      DEDGE(nodeList(start), nodeList(end))
    }
    val edgeList = (0 until edges).map(randomEdge).distinct
    Graph[NODE, DEDGE](nodeList, edgeList)
  }

  test("areNeighbors returns correct values for a 3-node graph") {
    val g = graph(1 -> 2, 1 -> 3)

    assert(g.areNeighbors(NODE(1), NODE(2)))
    assert(g.areNeighbors(NODE(1), NODE(3)))
    assert(!g.areNeighbors(NODE(2), NODE(3)))

    assert(!g.areNeighbors(NODE(2), NODE(1)))
    assert(!g.areNeighbors(NODE(3), NODE(1)))
    assert(!g.areNeighbors(NODE(3), NODE(2)))
  }

  test("Graph with only one edge has two nodes") {
    val g = graph( 1 -> 2 )
    assert( g.nodes.toSeq.contains( NODE(1) ))
    assert( g.nodes.toSeq.contains( NODE(2) ))

  }

  test("nodesWithInDegree returns 0-degree nodes correctly") {
    val g = graph( 1 -> 2, 1 -> 3, 2 -> 3, 3 -> 4 )
    assert(GraphAlgorithms.nodesWithInDegree(g, 0) === Set(NODE(1)))
  }

  test("topologicalSort returns nodes in correct order") {
    val g = graph( 1 -> 3, 1 -> 2, 3 -> 2, 2 -> 4 )
    assert( GraphAlgorithms.topologicalSort(g).map(_.name) === Seq(1, 3, 2, 4))
  }

  test("topologicalSort on a random graph produces the right ordering") {
    val rseed = new Random()
    //val seed = rseed.nextLong()
    val seed = 5220839347594113050L
    println("Seed %d".format(seed))
    val r = new Random(seed)
    val g = randomDAG(r, 20, 50)
    val ordering : Seq[NODE] = GraphAlgorithms.topologicalSort(g)

    g.nodes.foreach {
      case n : NODE => assert(ordering.contains(n), "%s wasn't in the ordering".format(n))
    }

    ordering.zipWithIndex.foreach {
      case (orderedLater : NODE, j : Int) =>
        (0 until j).foreach { i =>
          val orderedEarlier = ordering(i)
          assert(!g.areNeighbors(orderedLater, orderedEarlier),
            "edge %s -> %s found, but %s comes before %s in the ordering"
              .format(orderedLater, orderedEarlier, orderedEarlier, orderedLater))
        }
    }
  }
}
