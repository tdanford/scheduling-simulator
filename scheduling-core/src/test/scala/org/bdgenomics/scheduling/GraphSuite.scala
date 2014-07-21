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

  test("areNeighbors returns correct values for a 3-node graph") {
    val g : DirectedGraph[NODE, DEDGE]= Graph(
      DEDGE(NODE("1"), NODE("2")),
      DEDGE(NODE("1"), NODE("3")))

    assert(g.areNeighbors(NODE("1"), NODE("2")))
    assert(g.areNeighbors(NODE("1"), NODE("3")))
    assert(!g.areNeighbors(NODE("2"), NODE("3")))

    assert(!g.areNeighbors(NODE("2"), NODE("1")))
    assert(!g.areNeighbors(NODE("3"), NODE("1")))
    assert(!g.areNeighbors(NODE("3"), NODE("2")))
  }
}
