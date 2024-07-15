package dev.architectury.extensions.injected.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface InjectedRegistryEntryExtension<T> {
    Holder<T> arch$holder();
    
    @Nullable
    default ResourceLocation arch$registryName() {
        return arch$holder().unwrapKey().map(ResourceKey::location).orElse(null);
    }
}
