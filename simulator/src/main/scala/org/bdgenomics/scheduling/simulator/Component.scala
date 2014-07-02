package org.bdgenomics.scheduling.simulator

/**
 * Component is a template for a resource -- Resources are instantiated by a Provisioner
 * from a Component.
 *
 * @param name The distinct name of the component (e.g. "aws m3.large")
 * @param cost The cost-per-unit-time of the resource
 * @param reliability A generic reliability parameter, probably roughly equivalent to "chance
 *                    of failure per unit time"
 * @param timeToStart The time (possibly average time?) it takes to start a resource of this Component type
 */
case class Component(name: String, cost: Double, reliability: Double, timeToStart: Long) {
}
