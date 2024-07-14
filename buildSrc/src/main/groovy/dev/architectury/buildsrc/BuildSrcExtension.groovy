package dev.architectury.buildsrc

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer;

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
            //project.loom.createRemapConfigurations(sourceSet)
        }
        project.logger.info("Setup common project")
    }
}
