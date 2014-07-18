/*
 * Copyright 2014 Timothy Danford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.scheduling

trait RandomNumberGenerator {
  def nextGaussian() : Double
  def nextDouble() : Double
  def nextInt(n : Int) : Int
  def nextLong() : Long
  def copy() : RandomNumberGenerator
}

class JavaRNG(seed : Option[Long] = None) extends RandomNumberGenerator {

  val random = seed.map { s => new java.util.Random(s) } getOrElse { new java.util.Random() }
  private var _state = random.nextLong()

  private def updateState() {
    random.setSeed(_state)
    _state = random.nextLong()
  }

  def nextGaussian() : Double = {
    updateState()
    random.nextGaussian()
  }

  def nextDouble() : Double = {
    updateState()
    random.nextDouble()
  }

  def nextInt(n : Int) : Int = {
    updateState()
    random.nextInt(n)
  }

  def nextLong() : Long = {
    updateState()
    random.nextLong()
  }

  def copy() : RandomNumberGenerator = new JavaRNG(Some(_state))
}
