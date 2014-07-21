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

trait Edge[Node] {
  def from : Node
  def to : Node
}

case class DEDGE( from : NODE, to : NODE ) extends Edge[NODE] {}
case class NODE( name : String ) {}

trait DirectedGraph[NodeType,EdgeType <: Edge[NodeType]] {

  def addNodes(nodes : Seq[NodeType]) : DirectedGraph[NodeType,EdgeType]
  def addEdges(edges : Seq[EdgeType]) : DirectedGraph[NodeType,EdgeType]

  def outEdges(node : NodeType) : Iterable[EdgeType]

  def areNeighbors( nFrom : NodeType, nTo : NodeType ) : Boolean =
    outEdges(nFrom).find( _.to == nTo ).nonEmpty
}

object Graph {

  def apply[NodeType, EdgeType <: Edge[NodeType]](edges : EdgeType*) : DirectedGraph[NodeType, EdgeType] =
    MemoryDirectedGraph(edges.distinct.groupBy(_.from))
}

case class MemoryDirectedGraph[NodeType, EdgeType <: Edge[NodeType]](adjacencies : Map[NodeType, Seq[EdgeType]])
  extends DirectedGraph[NodeType, EdgeType] {

  override def addNodes(nodes: Seq[NodeType]): DirectedGraph[NodeType, EdgeType] =
    MemoryDirectedGraph(adjacencies ++
      nodes.filter(!adjacencies.contains(_)).map(n => (n, Seq())).toMap )

  override def outEdges(node: NodeType): Iterable[EdgeType] =
    adjacencies.getOrElse(node, Iterable())

  override def addEdges(edges: Seq[EdgeType]): DirectedGraph[NodeType, EdgeType] = {
    val existingFlat : Seq[EdgeType] = adjacencies.toSeq.flatMap( _._2 )
    val newFlat : Seq[EdgeType] =
      edges.filter(e => adjacencies.contains(e.from) && adjacencies.contains(e.to))

    val newMap : Map[NodeType, Seq[EdgeType]] =
      (existingFlat ++ newFlat).distinct.groupBy( _.from )

    MemoryDirectedGraph(newMap)
  }
}

