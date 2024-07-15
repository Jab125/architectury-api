package dev.architectury.buildsrc.transformer;

import dev.architectury.transformer.Transform;
import dev.architectury.transformer.input.FileAccess;
import dev.architectury.transformer.shadowed.impl.com.google.gson.Gson;
import dev.architectury.transformer.shadowed.impl.com.google.gson.GsonBuilder;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
import dev.architectury.transformer.transformers.BuiltinProperties;
import dev.architectury.transformer.transformers.base.AssetEditTransformer;
import dev.architectury.transformer.transformers.base.edit.TransformerContext;
import dev.architectury.transformer.util.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.function.UnaryOperator;

public class AddRefmapName2 implements AssetEditTransformer {
    private final String module;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public AddRefmapName2(String module) {
        this.module = module;
    }
    @Override
    public void doEdit(TransformerContext context, FileAccess output) throws Exception {
        if (System.getProperty(BuiltinProperties.REFMAP_NAME) == null) return;
        var refmap = module + "-" + System.getProperty(BuiltinProperties.REFMAP_NAME);
        var mixins = new HashSet<String>();
        output.handle((path, bytes) -> {
            // Check JSON file in root directory
            if (path.endsWith(".json") && !Transform.trimLeadingSlash(path)
                    .contains("/") && !Transform.trimLeadingSlash(path).contains("\\")
            ) {
                Logger.debug("Checking whether $path is a mixin config.");
                try {
                    var json = gson.fromJson(new String(bytes), JsonObject.class);
                    if (json != null) {
                        var hasMixins = json.has("mixins") && json.get("mixins").isJsonArray();
                        var hasClient = json.has("client") && json.get("client").isJsonArray();
                        var hasServer = json.has("server") && json.get("server").isJsonArray();
                        if (json.has("package") && (hasMixins || hasClient || hasServer)) {
                            if (!json.has("refmap") || !json.has("minVersion")) {
                                mixins.add(path);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
        if (!mixins.isEmpty()) {
            Logger.debug("Found mixin config(s): " + java.lang.String.join(",", mixins));
        }
        mixins.forEach(path ->
                modifyFile(output, path, bytes -> {
                    JsonObject json = gson.fromJson(
                            new String(bytes),
                            JsonObject.class
                    );
                    
                    if (!json.has("refmap")) {
                        Logger.debug("Injecting $refmap to $path");
                        json.addProperty("refmap", refmap);
                    }
                    
                    return gson.toJson(json).getBytes(StandardCharsets.UTF_8);
                }));
    }
    
    private byte[] modifyFile(FileAccess access, String path, UnaryOperator<byte[]> operator) {
        try {
            return access.modifyFile(path, operator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
