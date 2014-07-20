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

class EventSuite extends FunSuite {

  test("InitialEvent adds an EventSource to a Simulator's sources list") {
    object TestEventSource extends EventSource {
      override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = None
    }

    case class TestInitialEvent(time : Long, source : EventSource)
      extends InitialEvent[EventSource](time, source) {}

    val testInitialEvent = TestInitialEvent(10, TestEventSource)

    val sim : Simulator = new Simulator(new Parameters(), new EventHistory(), Seq())
    val updatedSim : Option[Simulator] = testInitialEvent.execute(sim)

    assert( updatedSim.isDefined )
    assert( updatedSim.get.sources === Seq(TestEventSource) )
  }

  test("TerminalEvent removes an EventSource from a Simulator's sources list") {
    object TestEventSource extends EventSource {
      override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = None
    }

    case class TestTerminalEvent(time : Long, source : EventSource)
      extends TerminalEvent[EventSource](time, source) {}

    val testEvent = TestTerminalEvent(10, TestEventSource)

    val sim : Simulator = new Simulator(new Parameters(), new EventHistory(), Seq(TestEventSource))
    assert( sim.sources === Seq(TestEventSource) )

    val updatedSim : Option[Simulator] = testEvent.execute(sim)

    assert( updatedSim.isDefined )
    assert( updatedSim.get.sources === Seq() )
  }

}
