package io.github.lightman314.lightmanscurrency.datagen.util;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public class CustomPathProvider {
    private final Path root;
    private final String kind;

    public CustomPathProvider(@Nonnull PackOutput output, @Nonnull PackOutput.Target target, @Nonnull String kind) { this(output,target,kind,null); }
    public CustomPathProvider(@Nonnull PackOutput output, @Nonnull PackOutput.Target target, @Nonnull String kind, @Nullable String subPack) {
        if(subPack != null)
            this.root = output.getOutputFolder().resolve(subPack).resolve(getDirectory(target));
        else
            this.root = output.getOutputFolder(target);
        this.kind = kind;
    }


    public Path file(@Nonnull ResourceLocation location, @Nonnull String extension) {
        return this.root.resolve(location.getNamespace()).resolve(this.kind).resolve(location.getPath() + "." + extension);
    }

    public Path json(@Nonnull ResourceLocation location) { return this.file(location, "json"); }

    //Manual copy of the directory enum value since it's private for reasons...
    private static String getDirectory(@Nonnull PackOutput.Target target)
    {
        return switch (target) {
            case RESOURCE_PACK -> "assets";
            case DATA_PACK -> "data";
            case REPORTS -> "reports";
        };
    }

}
