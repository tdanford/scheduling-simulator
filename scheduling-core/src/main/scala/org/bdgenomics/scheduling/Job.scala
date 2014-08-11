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

case class Task(name : String, size : Int) {
}

case class Job(id : String, task : Task, resource : Resource, timeStarted : Long) extends EventSource {

  override def sampleNextEvent(history: EventHistory, params: Parameters): Option[Event] = {
    val jobCompletedTime = params.sampleJobCompleteTime(task.size)
    if (params.sampleJobFailure())
      Some(JobFailed(history.currentTime + params.rng.nextLong() % jobCompletedTime, this))
    else
      Some(JobSucceeded(history.currentTime + jobCompletedTime, this))
  }
}

case class JobStarted(time : Long, job : Job, resource : Resource) extends InitialEvent[Job](time, job) {}

case class JobKilled(time : Long, job : Job, scheduler : Scheduler) extends TerminalEvent[Job](time, job) {
}

case class JobFailed(time : Long, job : Job) extends TerminalEvent[Job](time, job) {
}

case class JobSucceeded(time : Long, job : Job) extends TerminalEvent[Job](time, job) {
}
