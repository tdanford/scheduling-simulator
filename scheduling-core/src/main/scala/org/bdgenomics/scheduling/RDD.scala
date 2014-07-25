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

class RDDGraph(resultRDDs : Seq[RDD]) {



}

trait Dependency {
  def from : RDD
  def to : RDD
}

case class NarrowDependency(from : RDD, to : RDD) extends Dependency {}
case class WideDependency(from : RDD, to : RDD) extends Dependency {}

trait Partitioning {
  def partitions : Int
}

case class RDD(id : String, partition : Partitioning, deps : Seq[Dependency]) {}

/**
 *
 * @param rdds a sequence of consecutive RDDs, each depending (in a narrow dependency)
 *             on the RDD before it.
 * @param part the (consistent) index of the partition (in all the RDDs) of the data
 *             for this task.
 */
case class RDDTask(rdds : Seq[RDD], part : Int) {

}
