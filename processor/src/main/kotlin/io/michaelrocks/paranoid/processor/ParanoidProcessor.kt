package io.michaelrocks.paranoid.processor

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.paranoid.processor.commons.closeQuietly
import io.michaelrocks.paranoid.processor.io.IoFactory
import io.michaelrocks.paranoid.processor.logging.getLogger
import io.michaelrocks.paranoid.processor.model.Deobfuscator
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import java.io.File

class ParanoidProcessor(
        private val inputs: List<File>,
        private val outputs: List<File>,
        private val sourcePath: File,
        private val genPath: File,
        private val classpath: Collection<File>,
        private val bootClasspath: Collection<File>,
        private val projectName: String
) {
    private val logger = getLogger()

    private val grip: Grip = GripFactory.create(inputs + classpath + bootClasspath)
    private val stringRegistry = StringRegistryImpl()

    fun process() {
        dumpConfiguration()

        require(inputs.size == outputs.size) {
            "Input collection $inputs and output collection $outputs have different sizes"
        }

        val analysisResult = Analyzer(grip).analyze(inputs)
        analysisResult.dump()

        val deobfuscator = createDeobfuscator()
        logger.info("Prepare to generate {}", deobfuscator)

        val sourcesAndSinks = inputs.zip(outputs) { input, output ->
            IoFactory.createFileSource(input) to IoFactory.createFileSink(input, output)
        }

        try {
            Patcher(deobfuscator, stringRegistry, analysisResult, grip.classRegistry)
                    .copyAndPatchClasses(sourcesAndSinks)
            Generator(deobfuscator, stringRegistry)
                    .generateDeobfuscator(sourcePath, genPath, outputs + classpath, bootClasspath)
        } finally {
            sourcesAndSinks.forEach { (source, sink) ->
                source.closeQuietly()
                sink.closeQuietly()
            }
        }
    }

    private fun dumpConfiguration() {
        logger.info("Starting ParanoidProcessor:")
        logger.info("  inputs        = {}", inputs)
        logger.info("  outputs       = {}", outputs)
        logger.info("  sourcePath    = {}", sourcePath)
        logger.info("  genPath       = {}", genPath)
        logger.info("  classpath     = {}", classpath)
        logger.info("  bootClasspath = {}", bootClasspath)
        logger.info("  projectName   = {}", projectName)
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

    private fun createDeobfuscator(): Deobfuscator {
        val deobfuscatorInternalName = "io/michaelrocks/paranoid/Deobfuscator${composeDeobfuscatorNameSuffix()}"
        val deobfuscatorType = getObjectTypeByInternalName(deobfuscatorInternalName)
        val deobfuscationMethod = Method("getString", Type.getType(String::class.java), arrayOf(Type.INT_TYPE))
        return Deobfuscator(deobfuscatorType, deobfuscationMethod)
    }

    private fun composeDeobfuscatorNameSuffix(): String {
        val normalizedProjectName = projectName.filter { it.isLetterOrDigit() || it == '_' || it == '$' }
        return if (normalizedProjectName.isEmpty() || normalizedProjectName.startsWith('$')) {
            normalizedProjectName
        } else {
            '$' + normalizedProjectName
        }
    }
}
