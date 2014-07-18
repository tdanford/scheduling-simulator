/**
 * Copyright 2014 Timothy Danford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.scheduling

import scala.annotation.tailrec

/**
 * "This was my life at time t_1, this was my life at time t_2..."
 *
 * @param head The most recent event
 * @param tail The rest of the history before this event
 */
class EventHistory( val head : Event, val tail : Option[EventHistory] ) {
  def this() = this(StartEvent, None)
  def currentTime : Long = head.time
  def addToHistory( event : Event ) : EventHistory = new EventHistory(event, Some(this))
}

trait Parameters {
  def rng : RandomNumberGenerator

  def copy() : Parameters
}

trait RandomNumberGenerator {
  def nextDouble() : Double
  def nextInt(n : Int) : Int
  def nextLong() : Long
  def copy() : RandomNumberGenerator
}

class JavaRNG(seed : Option[Long] = None) extends RandomNumberGenerator {

  val random = seed.map { s => new java.util.Random(s) } getOrElse { new java.util.Random() }
  private var _state = random.nextLong()

  private def updateState() {
    random.setSeed(_state)
    _state = random.nextLong()
  }

  def nextDouble() : Double = {
    updateState()
    random.nextDouble()
  }

  def nextInt(n : Int) : Int = {
    updateState()
    random.nextInt(n)
  }

  def nextLong() : Long = {
    updateState()
    random.nextLong()
  }

  def copy() : RandomNumberGenerator = new JavaRNG(Some(_state))
}

trait EventSource {
  def sampleNextEvent( history : EventHistory, params : Parameters ) : Option[Event]
}

trait Event {
  def time : Long
  def source : EventSource

  def execute( sim : Simulator ) : Option[Simulator] = Option(sim)
}

/**
 * The default, STARTING event -- every EventHistory begins with this event at time=0
 */
object StartEvent extends Event {

  val time = 0L
  val source = null
}

/**
 * A Simulator is the (immutable) state of a simulation, including a set of event sources and an
 * event history (which includes the current time).
 *
 * @param params The parameters (including the state of the PRNG)
 * @param history The events which have already occurred; the current time is dictated by the state of the
 *                most recent event in the history.
 * @param sources The possible sources of future events.
 */
class Simulator(val params : Parameters, val history : EventHistory, val sources : Seq[EventSource]) {

  def copy() : Simulator = new Simulator(params.copy(), history, sources)

  /**
   * Samples the next state of the simulation from this (the current) one, and returns it -- or None,
   * if there are no more events to process.
   *
   * @return The next state of the simulation.
   */
  def simulateNextEvent() : Option[Simulator] = {
    val nextEvents = sources.flatMap {
      case source => source.sampleNextEvent( history, params )
    }.sortBy(_.time)

    nextEvents match {
      case first :: rest => first.execute(new Simulator(params, history.addToHistory(first), sources))
      case Seq() => None
    }
  }
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

