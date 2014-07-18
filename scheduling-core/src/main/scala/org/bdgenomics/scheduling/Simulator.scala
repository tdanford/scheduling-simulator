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

import scala.annotation.tailrec
import scala.math._

/**
 * A Simulator is the (immutable) state of a simulation, including a set of event sources and an
 * event history (which includes the current time).
 *
 * @param params The parameters (including the state of the PRNG)
 * @param history The events which have already occurred; the current time is dictated by the state of the
 *                most recent event in the history.
 * @param sources The possible sources of future events.
 */
class Simulator(val params : Parameters,
                val history : EventHistory,
                val sources : Seq[EventSource]) {

  def update( sourceUpdate : Seq[EventSource] => Seq[EventSource] ) : Simulator =
    new Simulator( params.copy(), history, sourceUpdate(sources))

  /**
   * Samples the next state of the simulation from this (the current) one, and returns it -- or None,
   * if there are no more events to process.
   *
   * @return The next state of the simulation.
   */
  def simulateNextEvent() : Option[Simulator] = {

    val nextEvents : Seq[Event] = sources.flatMap {
      case source => source.sampleNextEvent( history, params )
    }.sortBy(_.time)

    nextEvents match {
      case first :: rest => first.execute(new Simulator(params, history.addToHistory(first), sources))
      case Seq() => None
    }
  }
}

case class Parameters(rng : RandomNumberGenerator) {
  def sampleResourceFailureTime() : Long = rng.nextLong() % 10000
  def sampleJobFailure() : Boolean = rng.nextDouble() <= 0.1
  def sampleJobCompleteTime( size : Int ) : Int = max(1, round(rng.nextGaussian() * (size/10)).toInt)
  def sampleResourceStartupTime() : Int = round(rng.nextGaussian() * 10).toInt

  def copy() : Parameters = Parameters(rng.copy())
}

/**
 * A Timeline is a sequence of Simulator states, one for each event that was processed.
 *
 * @param startingPoint The starting simulation state.
 */
class Timeline(val startingPoint : Simulator) {

  @tailrec private def iterate( acc : Seq[Simulator], current : Simulator ) : Seq[Simulator] = {
    current.simulateNextEvent() match {
      case Some(nextSim) => iterate(current +: acc, nextSim)
      case None => acc
    }
  }

  val simulators = iterate(Seq(), startingPoint)
}

trait TimelineValuation {
  def value(timeline : Timeline) : Double
}

