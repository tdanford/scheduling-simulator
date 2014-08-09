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

import java.util.UUID

trait Stateful {
  def updateState(e : Event) : Stateful
}

abstract class Scheduler extends EventSource {
}

object Tracker {
  def apply[T <: EventSource]() : Tracker[T] = new Tracker[T](Map())
  def apply[T <: EventSource](history : EventHistory) : Tracker[T] =
    history.foldStateful(new Tracker[T](Map()))
}

class Tracker[T <: EventSource](val events : Map[T, (InitialEvent[T], Option[TerminalEvent[T]])]) extends Stateful {
  override def updateState(e: Event): Tracker[T] = {
    e match {
      case startEvent : InitialEvent[T] =>
        events.get(startEvent.started) match {
          case None =>
            new Tracker[T]( events ++ Seq(startEvent.started -> (startEvent, None) ) )

          case Some((initial : InitialEvent[T], ended : Option[TerminalEvent[T]])) =>
            throw new IllegalStateException("%s had already started by time %d".format(startEvent.started, startEvent.time))
        }

      case endEvent : TerminalEvent[T] =>
        events.get(endEvent.terminated) match {
          case Some((initial : InitialEvent[T], None)) =>
            new Tracker[T](events.updated(endEvent.terminated, (initial, Some(endEvent))))

          case Some((initial : InitialEvent[T], Some(terminal : TerminalEvent[T]))) =>
            throw new IllegalStateException("%s had already had a terminal event by time %d".format(endEvent.terminated, endEvent.time))
          case None =>
            throw new IllegalStateException("%s had not started by time %d".format(endEvent.terminated, endEvent.time))
        }

      case _ => this
    }
  }

  def findOpen() : Seq[T] = events.filter( p => isOpenPair(p._2) ).map(_._1).toSeq

  def duration(value : T) : Option[Long] = events.get(value).flatMap {
    case (initialEvent, None) => None
    case (initialEvent, terminalEvent) => terminalEvent.map(evt => evt.time - initialEvent.time)
    case _ => None
  }

  def findClosed() : Seq[T] = events.filter( p => !isOpenPair(p._2) ).map(_._1).toSeq

  def isOpenPair( eventPair : (InitialEvent[T], Option[TerminalEvent[T]]) ) : Boolean =
    eventPair match {
      case (initialEvent, None) => true
      case (initialEvent, terminalEvent) => false
      case _ => true
    }

  def isOpen(value : T) : Boolean = events.get(value).map(isOpenPair).getOrElse(true)

}

class PendingResources(val resourceRequests : Seq[ResourceRequest]) extends Stateful {
  override def updateState(e: Event): PendingResources = {
    e match {
      case RequestEvent(time, request: ResourceRequest) =>
        new PendingResources(request +: resourceRequests)
      case FulfillEvent(time, request: ResourceRequest) =>
        new PendingResources(resourceRequests.filter(_ != request))
      case _ => this
    }
  }
}


trait FlowChart[T <: Scheduler] {
  def findEvent( scheduler : T ) : Option[Event]
}

case class Choice[T <: Scheduler](option : T => Boolean,
                                  trueChoice : FlowChart[T],
                                  falseChoice : FlowChart[T]) extends FlowChart[T] {
  override def findEvent(scheduler: T): Option[Event] =
    if(option(scheduler)) trueChoice.findEvent(scheduler) else falseChoice.findEvent(scheduler)
}

case class Leaf[T <: Scheduler](event : Option[Event]) extends FlowChart[T] {
  override def findEvent(scheduler: T): Option[Event] = event
}


