package dev.architectury.buildsrc

import net.fabricmc.loom.LoomGradleExtension
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject

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
            //System.out.println(c)
        }
        project.logger.info("Setup platform project")
    }

    void setupTasks() {
        SourceSetContainer sourceSets = getProject().extensions.findByName("sourceSets") as SourceSetContainer
        getModules().each { module ->
            def task = project.tasks.register(module.key + "Jar", Jar) {
                setGroup("build")
                from sourceSets.maybeCreate(module.key).output
                destinationDirectory.set(new File(project.getLayout().getBuildDirectory().getAsFile().get(), "devlibs"))
                archiveClassifier.set(module.key)
            }

            def remapTask = project.tasks.register('remap' + module.key.capitalize() + "Jar", net.fabricmc.loom.task.RemapJarTask) {
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

    // tasks.register('loaderInfoJar', Jar) {
    //	from sourceSets.loaderInfo.output
    //	destinationDirectory = new File(project.buildDir, "devlibs")
    //	archiveClassifier = "loaderinfo"
    //}
}
