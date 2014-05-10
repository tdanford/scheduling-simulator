/**
 * Copyright 2014 Genome Bridge LLC
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
package com.tdanford.simulator

import java.util.UUID

case class Task(taskId : String, deps : Seq[Task], timing : TaskTimingModel, size : TaskSizingModel) {
}

object TaskUtils {

  def task(taskType : String, meanTime : Double, varTime : Double, failure : Double, size : Long, deps : Seq[Task]) : Task = {
    val timing = TaskTimingModel(meanTime, varTime, failure)
    val sizing = TaskSizingModel(size)
    val randomId = "%s-%s".format(taskType, UUID.randomUUID())
    Task(randomId, deps, timing, sizing)
  }
}

case class TaskTimingModel(mean : Double, variance : Double, failureProb : Double) {}

case class TaskSizingModel(bytes : Long) {}
