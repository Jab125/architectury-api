package dev.architectury.buildsrc.transformer;

import dev.architectury.transformer.input.FileAccess;
import dev.architectury.transformer.shadowed.impl.com.google.common.base.MoreObjects;
import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode;
import dev.architectury.transformer.transformers.BuiltinProperties;
import dev.architectury.transformer.transformers.TransformExpectPlatform;
import dev.architectury.transformer.transformers.base.AssetEditTransformer;
import dev.architectury.transformer.transformers.base.edit.TransformerContext;

import java.lang.reflect.Field;
import java.util.UUID;

import static dev.architectury.transformer.transformers.RemapInjectables.getUniqueIdentifier;

public class TransformExpectPlatform2 extends TransformExpectPlatform {
    
    private final boolean addInjectacles;
    
    public TransformExpectPlatform2(boolean addInjectacles) {
        this.addInjectacles = addInjectacles;
        System.setProperty(BuiltinProperties.UNIQUE_IDENTIFIER, "architectury_inject_architectury_common_3cdc9ae33d7647e089575d8d77c3b153");
    }
    // Edit to TransformExpectPlatform that doesn't add the injectables
    @Override
    public void doEdit(TransformerContext context, FileAccess output) throws Exception {
        try {
            Field uniqueIdentifier = TransformExpectPlatform.class.getDeclaredField("uniqueIdentifier");
            uniqueIdentifier.setAccessible(true);
            uniqueIdentifier.set(this, "architectury_inject_architectury_common_3cdc9ae33d7647e089575d8d77c3b153");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (addInjectacles) {
            super.doEdit(context, output);
        }
    }
    
    @Override
    public ClassNode doEdit(String name, ClassNode node) {
        try {
            Field uniqueIdentifier = TransformExpectPlatform.class.getDeclaredField("uniqueIdentifier");
            uniqueIdentifier.setAccessible(true);
            uniqueIdentifier.set(this, "architectury_inject_architectury_common_3cdc9ae33d7647e089575d8d77c3b153");
        } catch (Throwable t) {
            t.printStackTrace();
        }
       // System.out.println("editing class " + name);
        return super.doEdit(name, node);
    }
}
