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

package io.michaelrocks.paranoid.plugin

import com.android.build.api.transform.*
import io.michaelrocks.paranoid.processor.ParanoidProcessor
import org.gradle.api.Project

public class ParanoidTransform extends Transform {
  private final Project project

  ParanoidTransform(final Project project) {
    this.project = project
  }

  @Override
  void transform(final Context context, final Collection<TransformInput> inputs,
      final Collection<TransformInput> referencedInputs, final TransformOutputProvider outputProvider,
      final boolean isIncremental) throws IOException, TransformException, InterruptedException {
    final DirectoryInput directoryInput = inputs.first().directoryInputs.first()
    final File output = outputProvider.getContentLocation(
        directoryInput.name, EnumSet.of(QualifiedContent.DefaultContentType.CLASSES),
        EnumSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY)
    final Collection<File> classpath = referencedInputs.collectMany {
      it.directoryInputs.collect { it.file } + it.jarInputs.collect { it.file }
    }
    final def processor = new ParanoidProcessor(
        directoryInput.file,
        new File(context.temporaryDir, "src"),
        output,
        classpath,
        project.android.bootClasspath.toList()
    )
    try {
      processor.process()
    } catch (final Exception exception) {
      throw new TransformException(exception)
    }
  }

  @Override
  String getName() {
    return "paranoid"
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  Set<QualifiedContent.Scope> getScopes() {
    return EnumSet.of(QualifiedContent.Scope.PROJECT)
  }

  @Override
  Set<QualifiedContent.Scope> getReferencedScopes() {
    return EnumSet.of(
        QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        QualifiedContent.Scope.PROVIDED_ONLY
    )
  }

  @Override
  boolean isIncremental() {
    return false
  }
}
