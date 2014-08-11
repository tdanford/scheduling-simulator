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

import scala.annotation.tailrec

trait Edge[Node] {
  def from : Node
  def to : Node
}

case class DEDGE( from : NODE, to : NODE ) extends Edge[NODE] {}
case class NODE( name : Int ) {}

trait DirectedGraph[NodeType,EdgeType <: Edge[NodeType]] {

  def nodes : Iterable[NodeType]

  def addNodes(nodes : Seq[NodeType]) : DirectedGraph[NodeType,EdgeType]
  def addEdges(edges : Seq[EdgeType]) : DirectedGraph[NodeType,EdgeType]

  def outEdges(node : NodeType) : Iterable[EdgeType]
  def inEdges(node : NodeType) : Iterable[EdgeType]

  def neighbors( nFrom : NodeType ) : Iterable[NodeType] =
    outEdges(nFrom).map(_.to)

  def areNeighbors( nFrom : NodeType, nTo : NodeType ) : Boolean =
    outEdges(nFrom).exists( _.to == nTo )
}

trait Visitor[T] {
  def visit( t : T ) : Boolean
}

class Collector[T] extends Visitor[T] {

  private var values : Seq[T] = Seq()

  def list : Seq[T] = values

  def visit( t : T ) : Boolean = {
    values = values :+ t
    true
  }
}

class BFS[N, E<:Edge[N]]( graph : DirectedGraph[N, E] ) {

  import org.bdgenomics.scheduling.algorithms.GraphAlgorithms.neighbors

  def visitNodes( visitor : Visitor[N], startSet : Seq[N] )  = {
    @tailrec def visitNext( visited : Set[N], current : Seq[N] ) {
      val continue : Boolean = current.nonEmpty && current.forall( visitor.visit )
      if(continue) {
        val nextVisited : Set[N] = visited ++ current
        val next : Seq[N] = neighbors(graph, current).filter(!nextVisited.contains(_))
        visitNext( nextVisited, next )
      }
    }

    visitNext( Set(), startSet )
  }

}

class TopoSort[N, E<:Edge[N]]( graph : DirectedGraph[N, E] ) {

  def visitNodes( visitor : Visitor[N] )  = {

    def uncounter( nodes : Seq[N] ) : ((Int, N)) => (Int, N) =
      (pair : (Int, N)) =>
        (pair._1 - nodes.count(_ == pair._2), pair._2)

    @tailrec def acc( incoming : Seq[(Int, N)] ) {
      incoming takeWhile(_._1 == 0) match {
        case Nil => Nil
        case ns : Seq[(Int, N)] =>
          if(ns.map(_._2).forall(visitor.visit)) {
            val nbrs: Seq[N] = ns.flatMap { case (count: Int, n: N) => graph.neighbors(n)}
            acc(incoming.drop(ns.size).map(uncounter(nbrs)).sortBy(_._1))
          }
      }
    }

    val incoming : Seq[(Int, N)] =
      graph.nodes.map( n => (GraphAlgorithms.inDegree(graph, n), n) ).toSeq.sortBy(_._1)

    acc( incoming )
  }

}

object GraphAlgorithms {

  def inDegree[N, E<: Edge[N]](g : DirectedGraph[N, E], n : N) : Int =
    g.nodes.count( f => g.areNeighbors(f, n) )

  def outDegree[N, E <: Edge[N]]( g : DirectedGraph[N, E], n : N) : Int =
    g.outEdges(n).size

  def nodesWithInDegree[N, E<:Edge[N]](g : DirectedGraph[N, E], degree : Int) : Iterable[N] =
    g.nodes.filter( n => inDegree(g, n) == degree )

  def outEdges[N, E<:Edge[N]](g : DirectedGraph[N, E], nodes : Seq[N]) : Seq[E] =
    nodes.flatMap( g.outEdges ).distinct

  def neighbors[N, E<:Edge[N]](g : DirectedGraph[N, E], nodes : Seq[N]) : Seq[N] =
    nodes.flatMap(g.neighbors).distinct

  def topologicalSort[N, E <: Edge[N]](graph: DirectedGraph[N, E]): Seq[N] = {
    val collector = new Collector[N]()
    new TopoSort(graph).visitNodes(collector)
    collector.list
  }
}

object Graph {

  def apply[NodeType,EdgeType <: Edge[NodeType]](nodes : Seq[NodeType], edges : Seq[EdgeType]) : DirectedGraph[NodeType, EdgeType] =
    MemoryDirectedGraph(nodes.toSet, edges.distinct.groupBy(_.from))

  def apply[NodeType, EdgeType <: Edge[NodeType]](edges : Seq[EdgeType]) : DirectedGraph[NodeType, EdgeType] = {
    val nodes : Set[NodeType] = edges.flatMap( e => Seq(e.from, e.to) ).toSet
    MemoryDirectedGraph(nodes, edges.distinct.groupBy(_.from))
  }
}

case class MemoryDirectedGraph[NodeType, EdgeType <: Edge[NodeType]](nodes : Set[NodeType],
                                                                     adjacencies : Map[NodeType, Seq[EdgeType]])
  extends DirectedGraph[NodeType, EdgeType] {

  override def addNodes(newNodes: Seq[NodeType]): DirectedGraph[NodeType, EdgeType] =
    MemoryDirectedGraph(nodes ++ newNodes, adjacencies)

  override def outEdges(node: NodeType): Iterable[EdgeType] =
    adjacencies.getOrElse(node, Iterable())

  override def inEdges(node : NodeType) : Iterable[EdgeType] =
    adjacencies.flatMap {
      case (n : NodeType, es : Seq[EdgeType]) => es.filter( _.to == node )
    }

  override def addEdges(edges: Seq[EdgeType]): DirectedGraph[NodeType, EdgeType] = {
    val existingFlat : Seq[EdgeType] = adjacencies.toSeq.flatMap( _._2 )
    val newFlat : Seq[EdgeType] =
      edges.filter(e => adjacencies.contains(e.from) && adjacencies.contains(e.to))

    val newMap : Map[NodeType, Seq[EdgeType]] =
      (existingFlat ++ newFlat).distinct.groupBy( _.from )

    val newNodes = edges.flatMap(e => Seq(e.from, e.to)).distinct

    MemoryDirectedGraph(nodes ++ newNodes, newMap)
  }

  override def toString: String = adjacencies.toString
}

