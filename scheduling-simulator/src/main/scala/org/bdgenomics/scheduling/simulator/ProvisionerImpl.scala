package org.bdgenomics.scheduling.simulator

import org.bdgenomics.scheduling.simulator.events._

class ProvisionerImpl(world: World, params: Params) extends Provisioner {
  override def requestResource(c: Component): Option[Resource] = {
    val resource = world.createResource(c)
    world.event
      .sendIn(c.timeToStart)
      .notAfter[ResourceDead](rd => rd.resource == resource)
      .message(new ResourceAvailable(resource))
  }

  override def killResource(r: Resource): Boolean = {
    r.shutdown()
    true
  }
}
