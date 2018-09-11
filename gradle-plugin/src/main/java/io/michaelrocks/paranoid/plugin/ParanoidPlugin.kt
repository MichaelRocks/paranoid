package io.michaelrocks.paranoid.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException

class ParanoidPlugin : Plugin<Project> {
    private lateinit var extension: ParanoidExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("paranoid", ParanoidExtension::class.java)

        try {
            val android = project.extensions.getByName("android") as BaseExtension
            project.addDependencies(getDefaultConfiguration())
            android.registerTransform(ParanoidTransform(extension, android))
        } catch (exception: UnknownDomainObjectException) {
            throw GradleException("Paranoid plugin must be applied *AFTER* Android plugin", exception)
        }
    }

    private fun getDefaultConfiguration(): String {
        return if (PluginVersion.major >= 3) "implementation" else "compile"
    }

    private fun Project.addDependencies(configurationName: String) {
        val version = Build.VERSION
        dependencies.add(configurationName, "io.michaelrocks:paranoid-core:$version")
    }
}
