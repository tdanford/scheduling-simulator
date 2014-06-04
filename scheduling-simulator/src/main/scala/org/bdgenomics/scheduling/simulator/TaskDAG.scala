package org.bdgenomics.scheduling.simulator

case class TaskDAG {
  def setTaskFinished(value: Task) = ???

  def setTaskScheduled(value: Task, isScheduled: Boolean) = ???

  def getLiveTasks: Seq[Task] = ???

}
