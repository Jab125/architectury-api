package dev.architectury.buildsrc;

import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.*;

public record ProjectModule(BuildSrcExtension extension, String name, Reference<String> description, Set<String> mixins, /* @FabricOnly */Map<String, List<String>> entrypoints, Set<ProjectModule> dependencies) {
    public ProjectModule dependsOn(String module) {
        ProjectModule dependantModule = extension.module(module);
        dependencies.add(dependantModule);
        return this;
    }
    
    public ProjectModule dependsOn(String... modules) {
        for (String module : modules) {
            ProjectModule dependantModule = extension.module(module);
            dependencies.add(dependantModule);
        }
        return this;
    }
    
    public ProjectModule entrypoint(String entrypoint, String... classes) {
        entrypoints.computeIfAbsent(entrypoint, a -> new ArrayList<>()).addAll(List.of(classes));
        return this;
    }
    
    public ProjectModule description(String description) {
        this.description.set(description);
        return this;
    }
    
    public ProjectModule mixin(String... mixins) {
        this.mixins.addAll(List.of(mixins));
        return this;
    }
    
    public Set<String> getMixinsFor(String platform) {
        platform = platform.toLowerCase(Locale.ROOT);
        HashSet<String> strings = new HashSet<>();
        for (String mixin : mixins) {
            if (mixin.startsWith(platform + ":")) {
                strings.add(mixin.substring(platform.length() + 1));
            } else {
                if (!mixin.contains(":")) {
                    strings.add(mixin);
                }
            }
        }
        return strings;
    }
    
    public HashSet<ProjectModule> getTransitiveDependencies() {
        HashSet<ProjectModule> objects = new HashSet<>(dependencies);
        dependencies.forEach(o -> objects.addAll(o.getTransitiveDependencies()));
        return objects;
    }
    
    public String getJarTaskName() {
        return name + "Jar";
    }
    
    public String getRemapJarTaskName() {
        return "remap" + StringGroovyMethods.capitalize(getJarTaskName());
    }
    
    public String getShadowJarTaskName() {
        return "shadow" + StringGroovyMethods.capitalize(getJarTaskName());
    }
}
