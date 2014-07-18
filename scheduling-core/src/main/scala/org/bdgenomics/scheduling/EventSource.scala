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
 * EventHistory is a linked list of Events in reverse-chronological order (most recent event is
 * first).
 *
 * @param head The most recent event
 * @param tail The rest of the history before this event
 */
class EventHistory( val head : Event, val tail : Option[EventHistory] ) {
  def this() = this(StartEvent, None)
  def currentTime : Long = head.time
  def addToHistory( event : Event ) : EventHistory = new EventHistory(event, Some(this))

  @tailrec private def accumulateEvents(acc : Seq[Event], p : Option[Event=>Boolean] = None) : Seq[Event] =
    head match {
      case StartEvent => acc.reverse
      case _ => accumulateEvents( if(p.map(pred=>pred(head)).getOrElse(true)) head +: acc else acc )
    }

  @tailrec private def flatMapEvents[T](acc : Seq[T], p : Event=>Option[T]) : Seq[T] = {
    head match {
      case StartEvent => acc.reverse
      case _ => flatMapEvents( p(head).map[Seq[T]]( mapped => mapped +: acc ).getOrElse( acc ), p )
    }
  }

  @tailrec private def tailFold[T](accFold : T=>T, initial : T, folder : Event=>(T=>T)) : T =
    head match {
      case StartEvent => accFold(initial)
      case _ => tailFold( folder(head).compose(accFold), initial, folder )
    }

  def foldStateful(initial : Stateful) : Stateful =
    fold[Stateful](initial)( (stateful, evt) => stateful.updateState(evt) )

  def fold[T](initial : T)(folder : (T, Event) => T) : T =
    tailFold[T](t => t, initial, e => t => folder(t, e) )

  def flatMap[T]( p : Event=>Option[T] ) : Seq[T] = flatMapEvents(Seq(), p )
  def map[T](p : Event=>T) : Seq[T] = flatMapEvents(Seq(), (e : Event) => Some(p(e)))
  def filter(p : Event=>Boolean) : Seq[Event] = accumulateEvents(Seq(), Some(p))
  def asSeq() : Seq[Event] = accumulateEvents(Seq())
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

abstract class IntervalEvent extends Event {}

abstract class TerminalEvent[T <: EventSource](time : Long, val terminated : T) extends IntervalEvent {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(sim.update( _.filter(src => src != terminated) ))
}

abstract class InitialEvent[T <: EventSource](time : Long, val started : T) extends IntervalEvent {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(sim.update(started +: _))
}



