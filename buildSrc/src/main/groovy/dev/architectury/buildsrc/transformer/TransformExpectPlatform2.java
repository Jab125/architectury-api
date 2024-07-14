package dev.architectury.buildsrc.transformer;

import dev.architectury.transformer.input.FileAccess;
import dev.architectury.transformer.shadowed.impl.com.google.common.base.MoreObjects;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
import dev.architectury.transformer.transformers.BuiltinProperties;
import dev.architectury.transformer.transformers.TransformExpectPlatform;
import dev.architectury.transformer.transformers.base.AssetEditTransformer;
import dev.architectury.transformer.transformers.base.edit.TransformerContext;

import static dev.architectury.transformer.transformers.RemapInjectables.getUniqueIdentifier;

public class TransformExpectPlatform2 extends TransformExpectPlatform {
    
    // Edit to TransformExpectPlatform that doesn't add the injectables
    @Override
    public void doEdit(TransformerContext context, FileAccess output) {}
}
