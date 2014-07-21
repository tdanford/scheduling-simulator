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

class TrackerSuite extends FunSuite {

  test("an InitalEvent and TerminalEvent cause a single entry to be created in the Tracker") {
    object TestEventSource extends EventSource {
      override def sampleNextEvent(history: EventHistory,
                                   params: Parameters): Option[Event] = None
    }

    object TestStart extends InitialEvent[EventSource](10, TestEventSource) {
      override def time: Long = 10
      override def source: EventSource = TestEventSource
    }
    object TestEnd extends TerminalEvent[EventSource](20, TestEventSource) {
      override def time: Long = 20
      override def source: EventSource = TestEventSource
    }

    val eh = EventHistory(Seq(TestStart, TestEnd))
    assert( eh.asSeq() === Seq(TestStart, TestEnd) )

    val tracker = new Tracker[EventSource](Map())
    val updatedTracker = eh.foldStateful[Tracker[EventSource]](tracker)

    assert( updatedTracker.events.contains(TestEventSource) )
    assert( updatedTracker.events.size === 1 )
    assert( !updatedTracker.isOpen(TestEventSource) )

  }
}
