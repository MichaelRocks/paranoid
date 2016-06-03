/*
 * Copyright 2016 Michael Rozumyanskiy
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

import io.michaelrocks.grip.GripFactory
import io.michaelrocks.paranoid.processor.logging.getLogger
import java.io.File

class ParanoidProcessor(
    private val inputPath: File,
    private val sourcePath: File,
    private val outputPath: File,
    private val classpath: Collection<File>,
    private val bootClasspath: Collection<File>
) {
  private val logger = getLogger()

  private val grip = GripFactory.create(listOf(inputPath) + classpath + bootClasspath)
  private val stringRegistry = StringRegistryImpl()

  fun process() {
    val analysisResult = Analyzer(grip).analyze(inputPath)
    analysisResult.dump()
    Patcher(stringRegistry).copyAndPatchClasses(inputPath, outputPath, analysisResult)
    Generator(stringRegistry).generateDeobfuscator(sourcePath, outputPath, classpath, bootClasspath)
  }

  private fun AnalysisResult.dump() {
    if (configurationsByType.isEmpty()) {
      logger.info("No classes to obfuscate")
    } else {
      logger.info("Classes to obfuscate:")
      configurationsByType.keys.forEach {
        logger.info("  {}", it)
      }
    }
  }
}
