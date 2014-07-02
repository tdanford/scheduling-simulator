package org.bdgenomics.scheduling.simulator

/**
 * Like Component, Task is a template for a Job.  A Resource takes a
 * Task and instantiates a Job from the Task, running on the resource.
 * @param size The "size" of the Task, not specified whether this is in terms of
 *             time or bytes
 * @param failureRate The probability-per-unit-time of failure
 */
case class Task(size: Int, failureRate: Double) {

}
