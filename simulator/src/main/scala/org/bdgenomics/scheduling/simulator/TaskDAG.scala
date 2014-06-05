package org.bdgenomics.scheduling.simulator

import collection.mutable

case class TaskDAG(graph: Graph[Task]) {
  private val taskStatus: mutable.Set[Task] = new mutable.HashSet[Task]()

  def setTaskFinished(value: Task) = {
    taskStatus.remove(value)
  }

  def setTaskScheduled(value: Task, isScheduled: Boolean) = if (isScheduled) {
    graph.roots.filter(_.value == value).headOption match {
      case None => throw new IllegalArgumentException("Task is not a root task.")
      case Some(n) => graph.remove(n)
    }
  } else {
    graph.insert(value)
  }
  def getLiveTasks: Seq[Task] = graph.roots.filterNot(r => taskStatus.contains(r.value)).toSeq.map(_.value)
}
