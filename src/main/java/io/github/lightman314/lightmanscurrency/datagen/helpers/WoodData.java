package io.github.lightman314.lightmanscurrency.datagen.helpers;

import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A data expansion of {@link WoodType} containing data that's only relevant during data generation
 */
public final class WoodData {

    private final Supplier<? extends Item> log;
    private final Supplier<? extends Item> plank;
    private final Supplier<? extends Item> slab;
    private final Identifier logSideTexture;
    private final Identifier logTopTexture;
    private final Identifier plankTexture;
    private WoodData(Builder builder) {
        this.log = builder.log;
        this.plank = builder.plank;
        this.slab = builder.slab;
        this.logSideTexture = builder.logSideTexture;
        this.logTopTexture = builder.logTopTexture;
        this.plankTexture = builder.plankTexture;
    }

    @Nullable
    public Item getLog() { return get(this.log); }
    @Nullable
    public Item getPlank() { return get(this.plank); }
    @Nullable
    public Item getSlab() { return get(this.slab); }
    @Nullable
    public Identifier getLogSideTexture() { return this.logSideTexture; }
    @Nullable
    public Identifier getLogTopTexture() { return this.logTopTexture; }
    @Nullable
    public Identifier getPlankTexture() { return this.plankTexture; }

    @Nullable
    private static Item get(@Nullable Supplier<? extends Item> supplier) { return supplier == null ? null : supplier.get(); }

    public static Builder builder(WoodType type) { return new Builder(type); }

    public static class Builder {

        private final WoodType type;
        private Builder(WoodType type) { this.type = type; }

        private Supplier<? extends Item> log;
        private Supplier<? extends Item> plank;
        private Supplier<? extends Item> slab;
        private Identifier logSideTexture;
        private Identifier logTopTexture;
        private Identifier plankTexture;

        public Builder withLog(Supplier<? extends Item> log,Identifier logTexture) { return this.withLog(log,logTexture,logTexture.withSuffix("_top")); }
        public Builder withLog(Supplier<? extends Item> log,Identifier logSideTexture,Identifier logTopTexture) {
            this.log = log;
            this.logSideTexture = logSideTexture;
            this.logTopTexture = logTopTexture;
            return this;
        }
        public Builder withPlanksAndSlab(Supplier<? extends Item> plank,Supplier<? extends Item> slab,Identifier plankTexture) {
            this.plank = plank;
            this.slab = slab;
            this.plankTexture = plankTexture;
            return this;
        }
        public Builder withPlanks(Supplier<? extends  Item> plank,Identifier plankTexture) {
            this.plank = plank;
            this.plankTexture = plankTexture;
            return this;
        }
        public Builder withSlab(Supplier<? extends Item> slab,Identifier plankTexture) {
            this.slab = slab;
            this.plankTexture = plankTexture;
            return this;
        }

        public WoodData build() {
            WoodData data = new WoodData(this);
            WoodHelper.register(this.type,data);
            return data;
        }

    }

}