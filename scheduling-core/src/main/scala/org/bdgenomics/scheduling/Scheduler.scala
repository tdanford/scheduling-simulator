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

abstract class Scheduler extends EventSource with Stateful {
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

/**
 * FulfillEvent executes a request and, by doing so, removes the corresponding Request (EventSource)
 * from the Simulators event source list.
 *
 * @param time
 * @param source
 */
case class FulfillEvent(time : Long, source : Request) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    source.action(sim).map(s => s.update( srcs => srcs.filter(src => src != source) ))
}

/**
 * RequestEvent puts an Request (a one-time EventSource) in to the EventSource list of the simulator.
 *
 * The order here is three-fold.  If a Scheduler (or other actor) wants to make a request, which
 * may take time to satisfy but _which is wishes to remember_ (for its own stateful purposes), it
 * needs to:
 * 1. return a RequestEvent from its own sampleNextEvent method
 * 2. the RequestEvent, when chosen for simulation, puts the Request _into the list of event sources_.
 *    At this point, the RequestEvent is a marker which the Scheduler can use to update its own state
 *    from the EventHistory, in the future.
 * 3. Finally, the Request proposes a single FulfillEvent.  When the FulfillEvent is accepted, the
 *    Request's action is performed and the Request is removed from the list of EventSources.
 *
 * @param time
 * @param source
 */
case class RequestEvent( time : Long, source : Request ) extends Event {
  override def execute(sim : Simulator) : Option[Simulator] =
    Some(sim.update(source +: _))
}

abstract class Request extends EventSource {

  def time : Long
  def requester : Any
  def action : Simulator=>Option[Simulator]

  def sampleNextEvent( history : EventHistory, params : Parameters ) : Option[Event] =
    Some(FulfillEvent(time, this))
}

case class ResourceRequest(time : Long,
                           requester : Scheduler,
                           resource : Resource) extends Request {
  def action : Simulator=>Option[Simulator] = {
    case sim : Simulator =>
      Some(sim.update( sources => resource +: sources ))
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


object SimpleScheduler {
  def hasRequestedResource(scheduler : SimpleScheduler) : Boolean =
    isResourcePending(scheduler) || isResourceStarted(scheduler)

  def isResourcePending(scheduler : SimpleScheduler) : Boolean =
    scheduler.pendingResources.resourceRequests.nonEmpty

  def isResourceStarted(scheduler : SimpleScheduler) : Boolean =
    scheduler.resourceTracker.events.keys.nonEmpty

  def isResourceAvailable(scheduler : SimpleScheduler) : Boolean =
    isResourceStarted(scheduler) &&
      scheduler.jobTracker.findOpen().isEmpty

}

/**
 * SimpleScheduler is a scheduler which, given a Provider, a Component, and a (linear) sequence of
 * Tasks, attempts to
 * (1) create a Resource
 * (2) execute each of the Tasks in sequence on the Resource
 * (3) and then shut down the Resource
 *
 * @param provider The provider from which to request the resource
 * @param component The component to request from the provider
 * @param tasks The linear sequence of tasks to execute
 * @param jobTracker The current state of the job execution (the events which have started and, optionally,
 *                   ended each job execution).
 * @param resourceTracker The current state of each resource (there should only be one key in this map)
 * @param pendingResources The currently pending resources requested by the scheduler.
 */
class SimpleScheduler(provider : Provider,
                      component : Component,
                      val tasks : Seq[Task],
                      val jobTracker : Tracker[Job],
                      val resourceTracker : Tracker[Resource],
                      val pendingResources : PendingResources) extends Scheduler with Stateful {

  def this(provider : Provider, component : Component, tasks : Seq[Task]) =
    this(provider, component, tasks,
      new Tracker[Job](Map()),
      new Tracker[Resource](Map()),
      new PendingResources(Seq()))

  lazy val nextTask : Option[Task] = tasks.find(!completedTask(_))

  def completedInterval( interval : (InitialEvent[Job], Option[TerminalEvent[Job]])) : Boolean =
    interval._2 match {
      case None => false
      case Some(js : JobSucceeded) => true
      case _ => false
    }

  def completedTask( t : Task ) : Boolean =
    jobTracker.events.keys.filter(_.task == t) // Find the jobs for this task...
      .flatMap(jobTracker.events.get)          // ... get their intervals and return
      .find(completedInterval).nonEmpty        // ... 'true' if any of their intervals are 'complete'

  def updateState(e : Event) : SimpleScheduler =
    new SimpleScheduler(provider, component, tasks,
      jobTracker.updateState(e),
      resourceTracker.updateState(e),
      pendingResources.updateState(e))

  override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = {

    val updated : SimpleScheduler =
      history.fold[SimpleScheduler](
        new SimpleScheduler(provider, component, tasks)
      )((sim, evt) => sim.updateState(evt))

    updated.findNextEvent(history, params)
  }

  def createId() : String = UUID.randomUUID().toString

  def requestResource(history : EventHistory, params : Parameters) : Event =
    RequestEvent(history.currentTime, ResourceRequest(history.currentTime, this,
      Resource(createId(), component, history.currentTime)))

  def findNextEvent(history: EventHistory, params: Parameters): Option[Event] = {

    /*
     * Used this code for reference, keeping it around to look at.
    val runningResources : Seq[Resource] = resourceTracker.findOpen()
    val runningJobs : Seq[Job] = jobTracker.findOpen()

    val resourceJobs : Map[Resource,Seq[Job]] =
      runningJobs.groupBy(j => jobTracker.events(j)._1.asInstanceOf[JobStarted].resource )
    val openResources : Seq[Resource] = runningResources.filter( r => !resourceJobs.contains(r) )
    */

    /**
     * 1. Has the Request for the Resource been filed?
     *    a) Yes? Proceed to (2)
     *    b) No? File it.
     * 2. Is the Resource started?
     *    a) Yes? Proceed to (3)
     *    b) No?  [Wait]
     * 3. Is the Resource available?
     *    a) Yes? Proceed to (4)
     *    b) No?  [Wait]
     * 4. Is there a task which has not been started yet?
     *    a) Yes?  Start it
     *    b) No?  Shutdown the resource
     */

    val step4 = Choice[SimpleScheduler]( s => s.nextTask.isDefined,
      Leaf(Some(
        JobStarted(
          history.currentTime,
          Job(createId(), nextTask.get, history.currentTime),
          resourceTracker.events.keys.head))),
      Leaf(Some(
        ShutdownEvent(history.currentTime, resourceTracker.events.keys.head))))

    val step3 = Choice[SimpleScheduler]( SimpleScheduler.isResourceAvailable, step4, Leaf(None))
    val step2 = Choice[SimpleScheduler]( SimpleScheduler.isResourceStarted, step3, Leaf(None))
    val step1 = Choice[SimpleScheduler]( SimpleScheduler.hasRequestedResource, step2,
      Leaf(Some(requestResource(history, params))))

    step1.findEvent(this)
  }
}
