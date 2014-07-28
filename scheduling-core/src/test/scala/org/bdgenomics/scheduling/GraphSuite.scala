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

import org.scalatest.FunSuite

class GraphSuite extends FunSuite {

  def graph( edges : (Int,Int)* ) : DirectedGraph[NODE, DEDGE] =
    Graph[NODE, DEDGE]( edges.map( p => DEDGE(NODE(p._1), NODE(p._2)) ) : _*)

  test("areNeighbors returns correct values for a 3-node graph") {
    val g = graph(1 -> 2, 1 -> 3)

    assert(g.areNeighbors(NODE(1), NODE(2)))
    assert(g.areNeighbors(NODE(1), NODE(3)))
    assert(!g.areNeighbors(NODE(2), NODE(3)))

    assert(!g.areNeighbors(NODE(2), NODE(1)))
    assert(!g.areNeighbors(NODE(3), NODE(1)))
    assert(!g.areNeighbors(NODE(3), NODE(2)))
  }

  test("nodesWithInDegree returns 0-degree nodes correctly") {
    val g = graph( 1 -> 2, 1 -> 3, 2 -> 3, 3 -> 4 )
    assert(GraphAlgorithms.nodesWithInDegree(g, 0) === Seq(NODE(1)))
  }

  test("topologicalSort returns nodes in correct order") {
    val g = graph( 1 -> 3, 1 -> 2, 3 -> 2, 2 -> 4 )
    assert( GraphAlgorithms.topologicalSort(g).map(_.name) === Seq(1, 3, 2, 4))
  }
}
