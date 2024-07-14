package dev.architectury.buildsrc.transformer;

import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;
import dev.architectury.transformer.transformers.RemapInjectables;

import java.lang.reflect.Field;

public class RemapInjectables2 extends RemapInjectables {
    public RemapInjectables2() {
        try {
            Field uniqueIdentifier = RemapInjectables.class.getDeclaredField("uniqueIdentifier");
            uniqueIdentifier.setAccessible(true);
            uniqueIdentifier.set(this, "architectury_inject_architectury_common_3cdc9ae33d7647e089575d8d77c3b153");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    @Override
    public void supplyProperties(JsonObject json) {
    
    }
}
