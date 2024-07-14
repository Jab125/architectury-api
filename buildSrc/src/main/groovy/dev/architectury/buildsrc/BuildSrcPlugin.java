package dev.architectury.buildsrc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

public class BuildSrcPlugin implements Plugin<PluginAware> {
    @Override
    public void apply(PluginAware target) {
        if (target instanceof Project project) {
            apply(project);
        }
    }
    
    public void apply(Project project) {
        project.getExtensions().create("buildSrc", BuildSrcExtension.class);
    }
}
