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

object TestEventSource extends EventSource {
  override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = None
}
case class TestEvent(time : Long, source : EventSource = TestEventSource) extends Event {}

class EventHistorySuite extends FunSuite {

  test("constructing an EventHistory directly from a Seq[Event] using " +
    "EventHistory.apply produces events in the right order") {

    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))

    val history = EventHistory(Seq(e1, e2, e3))

    assert(history.asSeq() === Seq(e1, e2, e3))
  }

  test("using the default constructor creates an EventHistory with a StartEvent") {
    val history = new EventHistory()
    assert( history.head === StartEvent )
  }

  test("adding events in non-chronological order triggers an IllegalArgumentException") {

    val (e1, e2, e3) = (TestEvent(1), TestEvent(3), TestEvent(2))

    intercept[IllegalArgumentException] {
      EventHistory(Seq(e1, e2, e3))
    }
  }

  test("asSeq returns Events in the order in which they're added") {

    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = new EventHistory().addToHistory(
      e1).addToHistory(
      e2).addToHistory(
      e3)

    assert( history.asSeq() === Seq(e1, e2, e3) )
  }

  test("flatMap maps and drops mapped values in the correct order") {
    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = new EventHistory().addToHistory(
      e1).addToHistory(
        e2).addToHistory(
        e3)

    def map(e : Event) : Option[Long] = e match {
      case TestEvent(2, TestEventSource) => None
      case e : Event => Some(e.time)
    }

    assert( history.flatMap(map) === Seq(1L, 3L) )
  }

  test("foldStateful correctly folds a Stateful value through the Events") {
    class Adder(val sum : Long) extends Stateful {
      override def updateState(e: Event): Stateful = new Adder(sum + e.time)
    }

    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = new EventHistory().addToHistory(
      e1).addToHistory(
        e2).addToHistory(
        e3)

    val finalAdder : Adder = history.foldStateful(new Adder(0L))

    assert(finalAdder.sum === 6)
  }

  test("foldStateful applies the events in correct temporal order") {
    class Adder(val sum : Long, val lastTime : Long = 0L) extends Stateful {
      override def updateState(e: Event): Stateful = {
        if(e.time < lastTime) {
          throw new IllegalStateException("out of order events")
        }
        new Adder(sum + e.time, e.time)
      }
    }

    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = EventHistory(Seq(e1, e2, e3))

    val adder = history.foldStateful[Adder](new Adder(0L))

    assert(adder.sum === 6)
    assert(adder.lastTime === 3)
  }

  test("filter returns only those Events passing a predicate") {
    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = new EventHistory().addToHistory(
      e1).addToHistory(
        e2).addToHistory(
        e3)

    def predicate(e : Event) : Boolean = e match {
      case TestEvent(2, TestEventSource) => false
      case _ => true
    }

    assert( history.filter(predicate) === Seq(e1, e3) )
  }
}
