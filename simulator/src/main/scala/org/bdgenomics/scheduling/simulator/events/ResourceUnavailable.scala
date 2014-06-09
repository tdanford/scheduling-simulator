package org.bdgenomics.scheduling.simulator.events

import org.bdgenomics.scheduling.simulator.Resource

object ResourceUnavailable {
  def notAfter(r: Resource): Event => Boolean = {
    case (rd: ResourceDead) => rd.resource == r
    case (rs: ResourceShutdown) => rs.resource == r
    case _ => false
  }
}
