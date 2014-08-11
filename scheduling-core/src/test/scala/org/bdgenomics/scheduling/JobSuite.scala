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
import scala.math._

class JobSuite extends FunSuite {

  test("10,000 simulator runs produce job failure rates equivalent to the parameter failure rate") {

    val failureRate = 0.1 // if the default Parameters rate changes, need to update this

    val testComponent = Component("test-component", 1.0)
    val testRec = Resource("test-resource", testComponent, 0)

    val task = Task("test-task", 1)
    val job = Job("test-job", task, testRec, 0)

    val failures = (0 until 10000).map {
      case i =>
        val sim = Simulator(job)
        val timeline = new Timeline(sim)
        val lastEvent : Event = timeline.currentSimulator.history.head
        lastEvent match {
          case JobSucceeded(time, succeededJob) => 0
          case JobFailed(time, failedJob) => 1
          case _ => throw new IllegalStateException("Unknown last event type %s".format(lastEvent.getClass.getSimpleName))
        }
    }.sum

    val frac : Double = failures.toDouble / 10000.0
    assert(abs(frac - failureRate) <= 0.1)
  }
}
