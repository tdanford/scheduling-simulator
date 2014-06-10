package org.bdgenomics.scheduling.simulator

trait Job {
  def task: Task
  def resource: Resource
}
