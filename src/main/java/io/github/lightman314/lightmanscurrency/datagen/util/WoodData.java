package io.github.lightman314.lightmanscurrency.datagen.util;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WoodData {

    private final Supplier<? extends ItemLike> logBlock;
    public final Item getLog() { return getFromSupplier(this.logBlock); }
    public final ResourceLocation logSideTexture;
    public final ResourceLocation logTopTexture;
    private final Supplier<? extends ItemLike> plankBlock;
    public final Item getPlank() { return getFromSupplier(this.plankBlock); }
    public final ResourceLocation plankTexture;
    private final Supplier<? extends ItemLike> slabBlock;
    public final Item getSlab() { return getFromSupplier(this.slabBlock); }

    @Nullable
    private Item getFromSupplier(@Nullable Supplier<? extends ItemLike> source)
    {
        if(source != null)
        {
            ItemLike il = source.get();
            if(il != null)
                return il.asItem();
        }
        return null;
    }

    private WoodData(Supplier<? extends ItemLike> logBlock, Supplier<? extends ItemLike> plankBlock, Supplier<? extends ItemLike> slabBlock, ResourceLocation logSideTexture, ResourceLocation logTopTexture, ResourceLocation plankTexture)
    {
        this.logBlock = logBlock;
        this.logSideTexture = logSideTexture;
        this.logTopTexture = logTopTexture;
        this.plankBlock = plankBlock;
        this.plankTexture = plankTexture;
        this.slabBlock = slabBlock;
    }

    public static WoodData of1(Supplier<Supplier<? extends ItemLike>> logBlock, Supplier<Supplier<? extends ItemLike>> plankBlock, Supplier<Supplier<? extends ItemLike>> slabBlock, ResourceLocation logSideTexture, ResourceLocation logTopTexture, ResourceLocation plankTexture) { return new WoodData(() -> logBlock.get().get(), () -> plankBlock.get().get(), () -> slabBlock.get().get(), logSideTexture, logTopTexture, plankTexture); }
    public static WoodData of1(Supplier<Supplier<? extends ItemLike>> logBlock, Supplier<Supplier<? extends ItemLike>> plankBlock, Supplier<Supplier<? extends ItemLike>> slabBlock, String logSideTexture, String logTopTexture, String plankTexture) { return of1(logBlock, plankBlock, slabBlock, VersionUtil.parseResource(logSideTexture), VersionUtil.parseResource(logTopTexture), VersionUtil.parseResource(plankTexture)); }
    public static WoodData of2(Supplier<? extends ItemLike> logBlock, Supplier<? extends ItemLike> plankBlock, Supplier<? extends ItemLike> slabBlock, ResourceLocation logSideTexture, ResourceLocation logTopTexture, ResourceLocation plankTexture) { return new WoodData(logBlock, plankBlock, slabBlock, logSideTexture, logTopTexture, plankTexture); }
    public static WoodData of2(Supplier<? extends ItemLike> logBlock, Supplier<? extends ItemLike> plankBlock, Supplier<? extends ItemLike> slabBlock, String logSideTexture, String logTopTexture, String plankTexture) { return of2(logBlock, plankBlock, slabBlock, VersionUtil.parseResource(logSideTexture), VersionUtil.parseResource(logTopTexture), VersionUtil.parseResource(plankTexture)); }
    public static WoodData of(ItemLike logBlock, ItemLike plankBlock, ItemLike slabBlock, ResourceLocation logSideTexture, ResourceLocation logTopTexture, ResourceLocation plankTexture) { return new WoodData(() -> logBlock, () -> plankBlock, () -> slabBlock, logSideTexture, logTopTexture, plankTexture); }
    public static WoodData of(ItemLike logBlock, ItemLike plankBlock, ItemLike slabBlock, String logSideTexture, String logTopTexture, String plankTexture) { return new WoodData(() -> logBlock, () -> plankBlock, () -> slabBlock, VersionUtil.parseResource(logSideTexture), VersionUtil.parseResource(logTopTexture), VersionUtil.parseResource(plankTexture)); }

}