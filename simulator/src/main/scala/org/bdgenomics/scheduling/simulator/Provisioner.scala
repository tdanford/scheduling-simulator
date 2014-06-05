package org.bdgenomics.scheduling.simulator

trait Provisioner {
  def requestResource(c: Component): Option[Resource]
  def killResource(r: Resource): Boolean
}
