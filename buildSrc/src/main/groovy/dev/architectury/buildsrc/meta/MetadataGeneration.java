package dev.architectury.buildsrc.meta;

import dev.architectury.buildsrc.ProjectModule;
import dev.architectury.buildsrc.meta.fabric.FabricModJson;
import dev.architectury.buildsrc.meta.fabric.FabricModJsonBuilder;
import dev.architectury.plugin.ModLoader;
import dev.architectury.transformer.shadowed.impl.com.google.gson.GsonBuilder;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonArray;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
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
                    "https://github.com/architectury/architectury-api/issues",
                    "https://github.com/architectury/architectury-api",
                    "https://architectury.github.io/architectury-documentations/"
            ));
            fabricModJsonBuilder.license("LGPL-3");
            fabricModJsonBuilder.environment("*");
            fabricModJsonBuilder.mixins(module.getMixinsFor(loader).toArray(String[]::new));
            fabricModJsonBuilder.entrypoints(module.entrypoints());
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
            JsonObject jsonObject = new JsonObject();
            JsonObject modmenu = new JsonObject();
            JsonArray badges = new JsonArray();
            badges.add("library");
            modmenu.add("badges", badges);
            modmenu.addProperty("parent", "architectury");
            jsonObject.add("modmenu", modmenu);
            fabricModJsonBuilder.custom(jsonObject);
            return new GsonBuilder().setPrettyPrinting().create().toJson(fabricModJsonBuilder);
        }
        return "error!";
    }
}
