package dev.architectury.buildsrc

import dev.architectury.buildsrc.transformer.TransformExpectPlatform2
import dev.architectury.plugin.ArchitectPluginExtension
import dev.architectury.plugin.ModLoader
import dev.architectury.plugin.TransformingTask
import dev.architectury.plugin.loom.LoomInterface
import dev.architectury.transformer.Transformer
import dev.architectury.transformer.input.OpenedFileAccess
import dev.architectury.transformer.transformers.TransformExpectPlatform
import net.fabricmc.loom.LoomGradleExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject
import java.util.jar.JarOutputStream
import java.util.jar.Manifest


public abstract class BuildSrcExtension {
    @Inject
    abstract Project getProject()

    private Map<String, ProjectModule> modules = new HashMap<>()

    static BuildSrcExtension get(Project project) {
        return project.extensions.findByName("buildSrc") as BuildSrcExtension
    }

    Map<String, ProjectModule> getModules() {
        if (getProject() == getProject().rootProject) return modules
        return get(getProject().rootProject).modules
    }

    ProjectModule module(String name) {
        if (getProject() != getProject().rootProject) throw new IllegalAccessException()
        return modules.computeIfAbsent(name, k -> createModule(name))
    }

    ProjectModule createModule(String name) {
        if (getProject() != getProject().rootProject) throw new IllegalAccessException()
        return new ProjectModule(get(getProject()), name, new HashSet<ProjectModule>())
    }

    void commonSetup() {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each {
            def name = it.key
            def module = it.value

            SourceSet sourceSet = sourceSets.maybeCreate(name)
            module.transitiveDependencies.each { i ->
                SourceSet dep = sourceSets.maybeCreate(i.name())
                sourceSet.compileClasspath += dep.compileClasspath
                sourceSet.runtimeClasspath += dep.runtimeClasspath
                sourceSet.compileClasspath += sourceSets.main.compileClasspath
                sourceSet.runtimeClasspath += sourceSets.main.runtimeClasspath
                project.dependencies.add(name + "Implementation", dep.output)
            }
            // I hate classloaders
            LoomGradleExtension.get(project).createRemapConfigurations(sourceSet)
        }
        project.logger.info("Setup common project")
    }

    void platformSetup() {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each {
            def name = it.key
            def module = it.value

            SourceSet sourceSet = sourceSets.maybeCreate(name)

            project.dependencies.add(name + "Implementation", (project.project(":common").extensions.findByName("sourceSets") as SourceSetContainer).findByName(name).output)
            module.transitiveDependencies.each { i ->
                SourceSet dep = sourceSets.maybeCreate(i.name())
                sourceSet.compileClasspath += dep.compileClasspath
                sourceSet.runtimeClasspath += dep.runtimeClasspath
                sourceSet.compileClasspath += sourceSets.main.compileClasspath
                sourceSet.runtimeClasspath += sourceSets.main.runtimeClasspath
                project.dependencies.add(name + "Implementation", (project.project(":common").extensions.findByName("sourceSets") as SourceSetContainer).findByName(i.name()).output)
                project.dependencies.add(name + "Implementation", dep.output)
            }
            // I hate classloaders
           LoomGradleExtension.get(project).createRemapConfigurations(sourceSet)
            //System.out.println(c)
        }
        project.logger.info("Setup platform project")
    }

    void setupTasks() {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each { module ->
            def task = project.tasks.register(module.value.jarTaskName, Jar) {
                setGroup("build")
                from sourceSets.maybeCreate(module.key).output
                destinationDirectory.set(new File(project.getLayout().getBuildDirectory().getAsFile().get(), "devlibs"))
                archiveClassifier.set(module.key)
            }

            def remapTask = project.tasks.register(module.value.remapJarTaskName, net.fabricmc.loom.task.RemapJarTask) {
                dependsOn task
                input = task.get().archiveFile
                archiveClassifier = module.key
                addNestedDependencies = false
            }
            project.getTasksByName("build", false).each { buildTask ->
                buildTask.dependsOn(remapTask)
            }
        }
    }

    LoomGradleExtension loom() {
        return LoomGradleExtension.get(project)
    }

    ArchitectPluginExtension architectury() {
        return project.extensions.findByName("architectury") as ArchitectPluginExtension
    }

    void setupArchitecturyPlugin(ArchitectPluginExtension.CommonSettings settings) {
        def buildTask = project.tasks.getByName("build")
        LoomInterface loom = GroovyWorkaround.loomInterface(project);
        //ArchitectPluginExtension.CommonSettings settings = null;
        getModules().each { module ->
            def jarTask = project.tasks.getByName(module.value.jarTaskName) as AbstractArchiveTask
            jarTask.archiveClassifier.set(module.key + "-dev")
            for (loader in settings.loaders) {
                project.configurations.maybeCreate("transformProduction${loader.titledId}")
                def transformProductionTask =
                        project.tasks.register(module.key + "transformProduction${loader.titledId}".capitalize(), TransformingTask.class) { TransformingTask it ->
                            it.group = "Architectury"
                            it.platform = loader.id
                            it.doFirst { _ ->
                                def transformers = new ArrayList<Transformer>(it.transformers.get())
                                transformers.removeIf(a -> a instanceof TransformExpectPlatform)
                                it.transformers.set(transformers)
                                it.transformers.add(new TransformExpectPlatform2())
                            }
                            loader.transformProduction.invoke(it, loom, settings)

                            if (settings.isForgeLike() && loader.id == "neoforge") {
                                it.addPost(ModLoader.Companion.applyNeoForgeForgeLikeProd(loom, settings))
                            }

                            it.archiveClassifier.set("${module.key}TransformProduction${loader.titledId}")
                            it.input.set(jarTask.archiveFile)

                            project.artifacts.add("transformProduction${loader.titledId}", it)
                            it.dependsOn(jarTask)
                            buildTask.dependsOn(it)
                        }

                def file = transformProductionTask.get().archiveFile.get().asFile
                if (file?.exists()) {
                    file.parentFile.mkdirs()
                    new JarOutputStream(new FileOutputStream(file), new Manifest()).close()
                }

                def remapJarTask = project.tasks.getByName(module.value.remapJarTaskName) { Jar it ->

                    it.archiveClassifier.set(module.key)
                    loom.setRemapJarInput(it, jarTask.archiveFile)
                    it.dependsOn(jarTask)
                    it.doLast {
                        {
                            if (architectury().addCommonMarker) {
                                def output = it.archiveFile.get().asFile

                                try(def access = OpenedFileAccess.ofJar(output.toPath())) {
                                    access.addFile("architectury.common.marker", "")
                                } catch (Throwable t) {
                                    t.printStackTrace()
                                    project.logger.warn("Failed to add architectury.common.marker to ${output.absolutePath}")
                                }
                            }
                        }
                    }
                } as Jar
            }
        }
    }

    // tasks.register('loaderInfoJar', Jar) {
    //	from sourceSets.loaderInfo.output
    //	destinationDirectory = new File(project.buildDir, "devlibs")
    //	archiveClassifier = "loaderinfo"
    //}
}
