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

class SimulatorSuite extends FunSuite {

  test("running a Simulator with a single Resource produces a failure event") {

    val testComponent = Component("test-component", 1.0)
    val resource = Resource("test", testComponent, 0)
    val params = new Parameters()

    val sim = new Simulator(params, EventHistory(Seq()), Seq(resource))
    val timeline = new Timeline(sim)

    val events = timeline.currentSimulator.history.asSeq()

    assert(events.size === 1)
    assert(events.head.isInstanceOf[FailureEvent])
    assert(events.head.asInstanceOf[FailureEvent].source === resource)
  }

  test("running a Simulator with 100 Resources results in all the resources eventually failing") {
    val testComponent = Component("test-component", 1.0)
    val resources : Seq[EventSource] = (0 until 100).map {
      case i => Resource("test_resource_%d".format(i), testComponent, 0)
    }
    val sim = new Simulator(new Parameters(), EventHistory(Seq()), resources)
    val msg = "Assertion failed; PRNG seed=%d".format(sim.params.rng.getSeed)

    val timeline = new Timeline(sim)

    val events = timeline.currentSimulator.history.asSeq()

    val failures : Set[String] = events.flatMap {
      case FailureEvent(time, source) => Some(source.id)
      case _ => None
    }.toSet

    assert(failures.size === resources.size, msg)
    assert( resources.filter(r => !failures.contains(r.asInstanceOf[Resource].id)).isEmpty, msg )
  }

  test("running a Simulator with 100 Jobs results in all the Jobs either succeeding or failing") {
    val testComponent = Component("test-component", 1.0)
    val testRec = Resource("test-resource", testComponent, 0)

    val testTask = Task("test-task", 1)
    val jobs : Seq[Job] = (0 until 100).map {
      case i => Job("test_job_%d".format(i), testTask, testRec, 0)
    }

    val jobStarts : Seq[JobStarted] = jobs.map {
      j=> JobStarted(0, j, testRec)
    }

    val sim = new Simulator(new Parameters(),
      EventHistory(Seq(jobStarts : _*)),
      Seq[EventSource](jobs : _*))

    val msg = "Assertion failed; PRNG seed=%d".format(sim.params.rng.getSeed)

    val timeline = new Timeline(sim)

    val tracker = Tracker[Job](timeline.currentSimulator.history)

    jobs.foreach {
      j =>
        assert( tracker.events.contains(j) )
        assert( !tracker.isOpen(j) )
    }
  }


}
