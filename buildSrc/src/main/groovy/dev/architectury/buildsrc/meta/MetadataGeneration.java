package dev.architectury.buildsrc.meta;

import dev.architectury.buildsrc.ProjectModule;
import dev.architectury.buildsrc.meta.fabric.FabricModJson;
import dev.architectury.buildsrc.meta.fabric.FabricModJsonBuilder;
import dev.architectury.plugin.ModLoader;
import dev.architectury.transformer.shadowed.impl.com.google.gson.GsonBuilder;
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MetadataGeneration {
    
    private final String loader;
    private final Project project;
    
    public MetadataGeneration(Project project, String loader) {
        this.project = project;
        this.loader = loader;
    }
    
    public String generateMetadata(ProjectModule module) {
        String description = module.description().get();
        if (loader.equals("fabric")) {
            FabricModJsonBuilder fabricModJsonBuilder = new FabricModJsonBuilder();
            fabricModJsonBuilder.schemaVersion(1);
            fabricModJsonBuilder.id("architectury_" + module.name().toLowerCase(Locale.ROOT));
            fabricModJsonBuilder.version("${version}");
            fabricModJsonBuilder.name("Architectury (" + module.name() + ")");
            fabricModJsonBuilder.description(module.description().get());
            fabricModJsonBuilder.authors(new String[]{"shedaniel"});
            fabricModJsonBuilder.contact(new FabricModJson.ContactInfo(
                    "a", "b", "c"
            ));
            fabricModJsonBuilder.license("License");
            fabricModJsonBuilder.environment("*");
            fabricModJsonBuilder.mixins(module.getMixinsFor(loader).toArray(String[]::new));
            fabricModJsonBuilder.entrypoints(Map.of());
            fabricModJsonBuilder.icon("icon.png");
            {
                HashMap<String, String> objectObjectHashMap = new HashMap<>();
                
                for (ProjectModule dependency : module.dependencies()) {
                    objectObjectHashMap.put("architectury_" + dependency.name().toLowerCase(Locale.ROOT), "${version}");
                }
                
                objectObjectHashMap.put("fabricloader", ">=0.15.11");
                objectObjectHashMap.put("minecraft", "~1.20.6-");
                objectObjectHashMap.put("fabric-api", ">=0.99.0");
                fabricModJsonBuilder.depends(objectObjectHashMap);
            }
            fabricModJsonBuilder.breaks(Map.of("optifabric", "<1.13.0"));
            return new GsonBuilder().setPrettyPrinting().create().toJson(fabricModJsonBuilder);
        }
        return "error!";
    }
}
