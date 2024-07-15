package dev.architectury.buildsrc.meta;

import dev.architectury.buildsrc.ProjectModule;
import dev.architectury.buildsrc.meta.fabric.FabricModJson;
import dev.architectury.buildsrc.meta.fabric.FabricModJsonBuilder;
import dev.architectury.buildsrc.meta.neoforge.NeoForgeModsToml;
import dev.architectury.plugin.ModLoader;
import dev.architectury.transformer.shadowed.impl.com.google.gson.GsonBuilder;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonArray;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
import org.gradle.api.Project;

import java.util.*;

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
        } else if (loader.toLowerCase(Locale.ROOT).equals("neoforge")) {
            HashMap<String, List<NeoForgeModsToml.Dependency>> dependencies = new HashMap<>();
            List<NeoForgeModsToml.Dependency> moduleDepList = dependencies.computeIfAbsent("architectury_" + module.name().toLowerCase(Locale.ROOT), a -> new ArrayList<>());
            moduleDepList.add(new NeoForgeModsToml.Dependency("minecraft", "required", "[1.20.6,)", "NONE", "BOTH"));
            moduleDepList.add(new NeoForgeModsToml.Dependency("neoforge", "required", "[20.6.98-beta,)", "NONE", "BOTH"));
            return  new NeoForgeModsToml(
                    "javafml",
                    "[1,)",
                    "https://github.com/shedaniel/architectury/issues",
                    "GNU LGPLv3",
                    List.of(new NeoForgeModsToml.Mod(
                            "architectury_" + module.name().toLowerCase(Locale.ROOT),
                            "${version}",
                            "Architectury (" + module.name() + ")",
                            "shedaniel",
                            module.description().get(),
                            "icon.png",
                            "LGPL-3"
                    )),
                    dependencies,
                    module.getMixinsFor("neoforge").stream().map(NeoForgeModsToml.Mixin::new).toList()
            ).serialize();
        }
        return "error!";
    }
}
