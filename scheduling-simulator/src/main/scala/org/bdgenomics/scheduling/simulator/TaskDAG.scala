package org.bdgenomics.scheduling.simulator

case class TaskDAG(graph: Graph[Task]) {
  def setTaskFailed(value: Task) = ???
  def setTaskFinished(value: Task) = ???
  def setTaskScheduled(value: Task, isScheduled: Boolean) = ???
  def getLiveTasks: Seq[Task] = ???
}
