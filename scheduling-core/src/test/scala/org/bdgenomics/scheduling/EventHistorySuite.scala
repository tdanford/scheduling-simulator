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

  test("asSeq returns Events in the order in which they're added") {

    val (e1, e2, e3) = (TestEvent(1), TestEvent(2), TestEvent(3))
    val history = new EventHistory().addToHistory(
      e1).addToHistory(
      e2).addToHistory(
      e3)

    assert( history.asSeq() === Seq(e1, e2, e3) )
  }

}
