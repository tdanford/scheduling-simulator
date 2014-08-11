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
package org.bdgenomics.scheduling.utils

import org.scalatest.FunSuite

class RandomNumberGeneratorSuite extends FunSuite {

  val base = new JavaRNG()

  test("generating a thousand ints from several copies produces identical results") {
    base.nextLong()
    base.nextDouble()
    base.nextGaussian()

    val copy1 = base.copy()
    val copy2 = base.copy()
    val copy3 = copy2.copy()

    val base_1000 = (0 until 1000).foreach { i => base.nextInt(100) }
    val copy1_1000 = (0 until 1000).foreach { i => copy1.nextInt(100) }
    val copy2_1000 = (0 until 1000).foreach { i => copy2.nextInt(100) }
    val copy3_1000 = (0 until 1000).foreach { i => copy3.nextInt(100) }

    assert(copy1_1000 === base_1000)
    assert(copy2_1000 === base_1000)
    assert(copy3_1000 === base_1000)
  }

  test("generating a thousand doubles from several copies produces identical results") {
    base.nextLong()
    base.nextDouble()
    base.nextGaussian()

    val copy1 = base.copy()
    val copy2 = base.copy()
    val copy3 = copy2.copy()

    val base_1000 = (0 until 1000).foreach { i => base.nextDouble() }
    val copy1_1000 = (0 until 1000).foreach { i => copy1.nextDouble() }
    val copy2_1000 = (0 until 1000).foreach { i => copy2.nextDouble() }
    val copy3_1000 = (0 until 1000).foreach { i => copy3.nextDouble() }

    assert(copy1_1000 === base_1000)
    assert(copy2_1000 === base_1000)
    assert(copy3_1000 === base_1000)
  }

  test("generating a thousand longs from several copies produces identical results") {
    base.nextLong()
    base.nextDouble()
    base.nextGaussian()

    val copy1 = base.copy()
    val copy2 = base.copy()
    val copy3 = copy2.copy()

    val base_1000 = (0 until 1000).foreach { i => base.nextLong() }
    val copy1_1000 = (0 until 1000).foreach { i => copy1.nextLong() }
    val copy2_1000 = (0 until 1000).foreach { i => copy2.nextLong() }
    val copy3_1000 = (0 until 1000).foreach { i => copy3.nextLong() }

    assert(copy1_1000 === base_1000)
    assert(copy2_1000 === base_1000)
    assert(copy3_1000 === base_1000)
  }

  test("generating a thousand normals from several copies produces identical results") {
    base.nextLong()
    base.nextDouble()
    base.nextGaussian()

    val copy1 = base.copy()
    val copy2 = base.copy()
    val copy3 = copy2.copy()

    val base_1000 = (0 until 1000).foreach { i => base.nextGaussian() }
    val copy1_1000 = (0 until 1000).foreach { i => copy1.nextGaussian() }
    val copy2_1000 = (0 until 1000).foreach { i => copy2.nextGaussian() }
    val copy3_1000 = (0 until 1000).foreach { i => copy3.nextGaussian() }

    assert(copy1_1000 === base_1000)
    assert(copy2_1000 === base_1000)
    assert(copy3_1000 === base_1000)
  }

}
