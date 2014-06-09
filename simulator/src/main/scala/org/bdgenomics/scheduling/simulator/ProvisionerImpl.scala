package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events._

class ProvisionerImpl(world: World, params: Params) extends Provisioner {
  override def requestResource(c: Component): Option[Resource] = {
    val resource = world.createResource(c)
    world.event
      .sendIn(c.timeToStart)
      .notAfter(ResourceUnavailable.notAfter(resource))
      .message(new ResourceAvailable(resource))
    Some(resource)
  }

  override def killResource(r: Resource): Boolean = {
    world.event
      .sendIn(0)
      .notAfter(ResourceUnavailable.notAfter(r))
      .message(new ResourceShutdown(r))
    r.shutdown()
    true
  }
}
