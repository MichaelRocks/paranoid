/*
 * Copyright 2017 Michael Rozumyanskiy
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

package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.paranoid.processor.logging.getLogger
import java.io.File

class ParanoidProcessor(
    private val inputs: List<File>,
    private val outputs: List<File>,
    private val sourcePath: File,
    private val genPath: File,
    private val classpath: Collection<File>,
    private val bootClasspath: Collection<File>
) {
  private val logger = getLogger()

  private val grip: Grip = GripFactory.create(inputs + classpath + bootClasspath)
  private val stringRegistry = StringRegistryImpl()

  fun process() {
    require(inputs.size == outputs.size) {
      "Input collection $inputs and output collection $outputs have different sizes"
    }

    val analysisResult = Analyzer(grip).analyze(inputs)
    analysisResult.dump()
    Patcher(stringRegistry, grip.classRegistry).copyAndPatchClasses(inputs, outputs, analysisResult)
    Generator(stringRegistry).generateDeobfuscator(sourcePath, genPath, outputs + classpath, bootClasspath)
  }

  private fun AnalysisResult.dump() {
    if (configurationsByType.isEmpty()) {
      logger.info("No classes to obfuscate")
    } else {
      logger.info("Classes to obfuscate:")
      configurationsByType.forEach {
        val (type, configuration) = it
        logger.info("  {}:", type.internalName)
        configuration.constantStringsByFieldName.forEach {
          val (field, string) = it
          logger.info("    {} = \"{}\"", field, string)
        }
      }
    }
  }
}
