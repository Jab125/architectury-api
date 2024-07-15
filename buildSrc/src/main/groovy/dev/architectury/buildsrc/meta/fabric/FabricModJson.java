package dev.architectury.buildsrc.meta.fabric;

import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public record FabricModJson(int schemaVersion,
                            String id,
                            String version,
                            String name,
                            String description,
                            String[] authors,
                            ContactInfo contact,
                            String license,
                            String environment,
                            String[] mixins,
                            Map<String, List<String>> entrypoints,
                            String icon,
                            Map<String, String> depends,
                            Map<String, String> breaks,
                            JsonObject custom) {
    public record ContactInfo(String issues, String sources, String homepage) {}
}

/* {
  "schemaVersion": 1,
  "id": "architectury-misc",
  "version": "${version}",
  "name": "Architectury",
  "description": "Features that have not been sorted into a module yet",
  "authors": [
    "shedaniel"
  ],
  "contact": {
    "issues": "https://github.com/architectury/architectury-api/issues",
    "sources": "https://github.com/architectury/architectury-api",
    "homepage": "https://architectury.github.io/architectury-documentations/"
  },
  "license": "LGPL-3",
  "environment": "*",
  "mixins": [
    "architectury-common.mixins.json",
    "architectury.mixins.json"
  ],
  "entrypoints": {
    "main": [
      "dev.architectury.platform.fabric.GameInstanceImpl::init"
    ],
    "server": [
      "dev.architectury.init.fabric.ArchitecturyServer::init"
    ],
    "client": [
      "dev.architectury.init.fabric.ArchitecturyClient::init"
    ],
    "modmenu": [
      "dev.architectury.compat.fabric.ModMenuCompatibility"
    ]
  },
  "icon": "icon.png",
  "depends": {
    "minecraft": "~1.20.6-",
    "fabricloader": ">=0.15.11",
    "fabric-api": ">=0.99.0"
  },
  "breaks": {
    "optifabric": "<1.13.0"
  },
  "custom": {
    "modmenu": {
      "badges": ["library"]
    }
  }
}
*/