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
import scala.math.max

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

  @tailrec private def accumulateEvents(acc : Seq[Event]) : Seq[Event] =
    head match {
      case StartEvent => acc.reverse
      case _ => accumulateEvents(head +: acc)
    }

  def asSeq() : Seq[Event] = accumulateEvents(Seq())
}

case class Parameters(rng : RandomNumberGenerator) {
  def sampleResourceFailureTime() : Long = rng.nextLong() % 10000
  def sampleJobFailure() : Boolean = rng.nextDouble() <= 0.1
  def sampleJobCompleteTime( size : Int ) : Int = max(1, rng.nextInt(7) + (size - 3))

  def copy() : Parameters = Parameters(rng.copy())
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

abstract class TerminalEvent[T <: EventSource](time : Long, source : T) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(new Simulator(sim.params.copy(), sim.history, sim.sources.filter(s => s != source)))
}

abstract class InitialEvent[T <: EventSource](time : Long, source : T) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(new Simulator(sim.params.copy(), sim.history, source +: sim.sources))
}


