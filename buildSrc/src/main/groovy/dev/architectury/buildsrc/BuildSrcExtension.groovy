package dev.architectury.buildsrc

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.buildsrc.meta.MetadataGeneration
import dev.architectury.buildsrc.transformer.AddRefmapName2
import dev.architectury.buildsrc.transformer.RemapInjectables2
import dev.architectury.buildsrc.transformer.TransformExpectPlatform2
import dev.architectury.plugin.ArchitectPluginExtension
import dev.architectury.plugin.ModLoader
import dev.architectury.plugin.TransformingTask
import dev.architectury.plugin.loom.LoomInterface
import dev.architectury.plugin.transformers.AddRefmapName
import dev.architectury.transformer.Transformer
import dev.architectury.transformer.input.OpenedFileAccess
import dev.architectury.transformer.transformers.RemapInjectables
import dev.architectury.transformer.transformers.TransformExpectPlatform
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.ModSettings
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.util.Constants
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
        return new ProjectModule(get(getProject()), name, new Reference<String>(""), new HashSet<String>(), new HashMap<String, List<String>>(), new HashSet<ProjectModule>())
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

    void platformSetup(String loader) {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each {
            def name = it.key
            def module = it.value

            SourceSet sourceSet = sourceSets.maybeCreate(name)

            project.dependencies.add(name + "Implementation", (project.project(":common").extensions.findByName("sourceSets") as SourceSetContainer).findByName(name).output)
            project.configurations.maybeCreate("shadow" + name.capitalize() + "Common")
            project.dependencies.add("shadow" + name.capitalize() + "Common", project.dependencies.project(path: ":common", configuration: "${name}TransformProduction${loader.capitalize()}"))
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

    void forgeSetup(String loader) {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each {
            def name = it.key
            def module = it.value

            SourceSet sourceSet = sourceSets.maybeCreate(name)

            project.dependencies.add(name + "Implementation", (project.project(":common").extensions.findByName("sourceSets") as SourceSetContainer).findByName(name).output)
            project.configurations.maybeCreate("shadow" + name.capitalize() + "Common")
            project.dependencies.add("shadow" + name.capitalize() + "Common", project.dependencies.project(path: ":common", configuration: "${name}TransformProduction${loader.capitalize()}"))

            def mod = loom().mods.maybeCreate(name)
            mod.sourceSet(sourceSet)
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
        def generateMetadata = project.task("generateMetadata")
        def remapJarTask = project.tasks.getByName("remapJar") as RemapJarTask
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each { module ->
            def task = project.tasks.register(module.value.jarTaskName, Jar) {
                setGroup("build")
                from sourceSets.maybeCreate(module.key).output
                destinationDirectory.set(new File(project.getLayout().getBuildDirectory().getAsFile().get(), "devlibs"))
                archiveClassifier.set(module.key)
            }

            getProject().artifacts(artifactHandler -> artifactHandler.add(Constants.Configurations.NAMED_ELEMENTS, task))


            def shadowTask = project.tasks.register(module.value.shadowJarTaskName, ShadowJar) {
                from task
                setGroup("shadow")
                exclude "architectury-common.accessWidener"
                exclude "architectury.common.json"
                configurations = [project.configurations.maybeCreate("shadow" + module.key.capitalize() + "Common")]
                archiveClassifier = "dev-shadow-" + module.key
            }

            def remapTask = project.tasks.register(module.value.remapJarTaskName, net.fabricmc.loom.task.RemapJarTask) {
                setGroup("build")
                dependsOn shadowTask
                input = shadowTask.get().archiveFile
                archiveClassifier = module.key
                addNestedDependencies = false
            }

            remapJarTask.nestedJars.from remapTask

            project.getTasksByName("build", false).each { buildTask ->
                buildTask.dependsOn(remapTask)
            }

            def generateModuleMetadata = project.task("generate" + module.key.capitalize() + "Metadata") {
                doLast {
                    def generation = new MetadataGeneration(project, "fabric")
                    project.file("src/" + module.key + "/resources/").mkdirs()
                    project.file("src/" + module.key + "/resources/fabric.mod.json").text = generation.generateMetadata(module.value)
                }
            }
            generateMetadata.dependsOn(generateModuleMetadata)
        }
    }

    LoomGradleExtension loom() {
        return LoomGradleExtension.get(project)
    }

    ArchitectPluginExtension architectury() {
        return project.extensions.findByName("architectury") as ArchitectPluginExtension
    }

    void setupCommonTransforms(ArchitectPluginExtension.CommonSettings settings) {
        def buildTask = project.tasks.getByName("build")
        LoomInterface loom = GroovyWorkaround.loomInterface(project);
        //ArchitectPluginExtension.CommonSettings settings = null;
        getModules().each { module ->
            def jarTask = project.tasks.getByName(module.value.jarTaskName) as AbstractArchiveTask
            jarTask.archiveClassifier.set(module.key + "-dev")
            for (loader in settings.loaders) {
                project.configurations.maybeCreate(module.key + "TransformProduction${loader.titledId}")
                def transformProductionTask =
                        project.tasks.register(module.key + "TransformProduction${loader.titledId}".capitalize(), TransformingTask.class) { TransformingTask it ->
                            it.group = "Architectury"
                            it.platform = loader.id
                            it.doFirst { _ ->
                                def transformers = new ArrayList<Transformer>(it.transformers.get())
                                transformers.removeIf(a -> a instanceof TransformExpectPlatform)
                                transformers.removeIf(a -> a instanceof RemapInjectables)
                                transformers.removeIf(a -> a instanceof AddRefmapName)
                                it.transformers.set(transformers)
                                it.transformers.add(new TransformExpectPlatform2(module.key == "annotations"))
                                it.transformers.add(new RemapInjectables2())
                                it.transformers.add(new AddRefmapName2(module.key))
                            }
                            loader.transformProduction.invoke(it, loom, settings)

                            if (settings.isForgeLike() && loader.id == "neoforge") {
                                it.addPost(ModLoader.Companion.applyNeoForgeForgeLikeProd(loom, settings))
                            }

                            it.archiveClassifier.set("${module.key}TransformProduction${loader.titledId}")
                            it.input.set(jarTask.archiveFile)

                            project.artifacts.add("${module.key}TransformProduction${loader.titledId}", it)
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

//    void setupPlatformTransforms() {
//        getModules().each {
////            project.shadowJar {
////                exclude "architectury-common.accessWidener"
////                exclude "architectury.common.json"
////
////                configurations = [project.configurations.shadowCommon]
////                archiveClassifier = "dev-shadow"
////            }
//        }
//    }
}
