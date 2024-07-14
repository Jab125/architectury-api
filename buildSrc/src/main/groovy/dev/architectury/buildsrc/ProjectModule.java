package dev.architectury.buildsrc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ProjectModule(BuildSrcExtension extension, String name, Set<ProjectModule> dependencies) {
    public ProjectModule dependsOn(String module) {
        ProjectModule dependantModule = extension.module(module);
        dependencies.add(dependantModule);
        return dependantModule;
    }
    
    public HashSet<ProjectModule> getTransitiveDependencies() {
        HashSet<ProjectModule> objects = new HashSet<>(dependencies);
        dependencies.forEach(o -> objects.addAll(o.getTransitiveDependencies()));
        return objects;
    }
}
