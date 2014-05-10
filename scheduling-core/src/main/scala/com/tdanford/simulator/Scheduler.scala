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

/**
 * The core trait for an elastic scheduler.
 *
 * The scheduler hears about three possible events:
 * (1) that a node (which it has presumably requested) is ready
 * (2) that a task (which it has presumably started) is finished successfully
 * (3) or that a task (which it has presumably started) has failed.
 */
trait Scheduler {

  def nodeReady(node : Node)
  def taskStatus(task : Task, status : String)
  def wakeUp(time : Long)
}
