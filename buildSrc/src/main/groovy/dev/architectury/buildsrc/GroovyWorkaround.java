package dev.architectury.buildsrc;

import dev.architectury.plugin.loom.LoomInterface;
import org.gradle.api.Project;

public class GroovyWorkaround {
    public static LoomInterface loomInterface(Project project) {
        return LoomInterface.Companion.get(project);
    }
}
