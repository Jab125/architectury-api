package dev.architectury.buildsrc.meta.neoforge;

import java.util.List;
import java.util.Map;

public record NeoForgeModsToml(String modLoader,
                               String loaderVersion,
                               String issueTrackerUrl,
                               String license,
                               List<Mod> mods,
                               Map<String, List<Dependency>> dependencies,
                               List<Mixin> mixins) {
    
    public record Mod(String modId,
                      String version,
                      String displayName,
                      String authors,
                      String description,
                      String logoFile,
                      String license) {}
    
    public record Dependency(String modId,
                             String type,
                             String versionRange,
                             String ordering,
                             String side) {}
    
    public record Mixin(String config) {}
    
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append("modLoader = \"" + modLoader + "\"\n");
        builder.append("loaderVersion = \"" + loaderVersion + "\"\n");
        builder.append("issueTrackerUrl = \"" + issueTrackerUrl + "\"\n");
        builder.append("license = \"" + license + "\"\n");
        
        for (Mod mod : mods) {
            builder.append("\n");
            builder.append("[[mods]]\n");
            builder.append("modId = \"" + mod.modId + "\"\n");
            builder.append("version = \"" + mod.version + "\"\n");
            builder.append("displayName = \"" + mod.displayName + "\"\n");
            builder.append("authors = \"" + mod.authors + "\"\n");
            builder.append("description = '''\n");
            mod.description.lines().forEach(line -> {
                builder.append(line + "\n");
            });
            builder.append("logoFile = \"" + mod.logoFile + "\"\n");
            builder.append("license = \"" + mod.license + "\"\n");
        }
        
        for (Map.Entry<String, List<Dependency>> entry : dependencies.entrySet()) {
            for (Dependency dependency : entry.getValue()) {
                builder.append("\n");
                builder.append("[[dependencies." + entry.getKey() + "]]\n");
                builder.append("modId = \"" + dependency.modId + "\"\n");
                builder.append("type = \"" + dependency.type + "\"\n");
                builder.append("versionRange = \"" + dependency.versionRange + "\"\n");
                builder.append("ordering = \"" + dependency.ordering + "\"\n");
                builder.append("side = \"" + dependency.side + "\"\n");
            }
        }
        
        for (Mixin mixin : mixins) {
            builder.append("\n");
            builder.append("[[mixins]]");
            builder.append("config = \"" + mixin.config + "\"\n");
        }
        
        return builder.toString();
    }
}
