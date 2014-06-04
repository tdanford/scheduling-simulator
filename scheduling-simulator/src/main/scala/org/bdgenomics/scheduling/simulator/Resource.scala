package org.bdgenomics.scheduling.simulator

trait Resource {
  def component: Component

  def execute(t: Task): Job
  def shutdown()
}
