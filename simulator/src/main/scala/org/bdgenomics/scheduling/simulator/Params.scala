package org.bdgenomics.scheduling.simulator

import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

trait Params {
  def components: Seq[Component]
  def taskDistribution(random: RandomGenerator): RealDistribution
  def taskFailureDistribution(generator: RandomGenerator): RealDistribution
  def resourceDistribution(random: RandomGenerator): RealDistribution
}
