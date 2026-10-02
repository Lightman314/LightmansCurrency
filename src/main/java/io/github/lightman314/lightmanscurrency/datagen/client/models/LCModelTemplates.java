package io.github.lightman314.lightmanscurrency.datagen.client.models;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.datagen.client.models.multi.TallModelTemplate;
import io.github.lightman314.lightmanscurrency.datagen.client.models.multi.TallWideModelTemplate;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LCModelTemplates {
    private LCModelTemplates() {}

    public static final ModelTemplate COIN_BLOCK = new ModelTemplate(Optional.of(LCApi.id("block/coin_block")),Optional.empty(),LCTextureSlots.MAIN);
    public static final ModelTemplate COIN_PILE = new ModelTemplate(Optional.of(LCApi.id("block/coin_pile")),Optional.empty(),LCTextureSlots.MAIN);
    public static final ModelTemplate DISPLAY_CASE = withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/display_case/base")),Optional.empty(),LCTextureSlots.WOOL),"display_case/").withCustomBlockKey(LCModelTemplates::getColoredKey);
    public static final ModelTemplate SINGLE_SHELF = withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/single_shelf/base")),Optional.empty(),LCTextureSlots.PLANK),"single_shelf/").withCustomBlockKey(LCModelTemplates::getWoodenKey);
    public static final ModelTemplate DOUBLE_SHELF = withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/double_shelf/base")),Optional.empty(),LCTextureSlots.PLANK),"double_shelf/").withCustomBlockKey(LCModelTemplates::getWoodenKey);
    public static final ModelTemplate CARD_DISPLAY = withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/card_display/base")),Optional.empty(),LCTextureSlots.WOOL,LCTextureSlots.LOG,LCTextureSlots.LOG_TOP,LCTextureSlots.PLANK),"card_display/").withCustomBlockKey(LCModelTemplates::getWoodenAndColoredKey);

    public static final TallModelTemplate VENDING_MACHINE = TallModelTemplate.create(type ->
        withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/vending_machine/base" + type)),Optional.of(type),LCTextureSlots.INTERIOR,LCTextureSlots.EXTERIOR),"vending_machine/").withCustomBlockKey(LCModelTemplates::getColoredKey)
    );
    public static final TallWideModelTemplate LARGE_VENDING_MACHINE = TallWideModelTemplate.create(type ->
        withPrefix(new ModelTemplate(Optional.of(LCApi.id("block/large_vending_machine/base" + type)),Optional.of(type),LCTextureSlots.INTERIOR,LCTextureSlots.EXTERIOR),"large_vending_machine/").withCustomBlockKey(LCModelTemplates::getColoredKey)
    );

    public static final ModelTemplate WALLET_HIP = withPrefix(new ModelTemplate(Optional.of(LCApi.id("item/wallet_hip/wallet_base")),Optional.empty(),LCTextureSlots.MAIN),"wallet_hip/");

    public static PrefixTemplate withPrefix(ModelTemplate template,String prefix) { return new PrefixTemplate(template,prefix); }

    public static Identifier getColoredKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPath(LCTexturedModels.currentColor.getResourceSafeName());
    }
    public static Identifier getColoredKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).withPath(LCTexturedModels.currentColor.getResourceSafeName());
    }

    public static Identifier getWoodenKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPath(LCTexturedModels.currentWood.generatePath(""));
    }

    public static Identifier getWoodenKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).withPath(LCTexturedModels.currentWood.generatePath(""));
    }

    public static Identifier getWoodenAndColoredKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPath(LCTexturedModels.currentWood.generatePath("","/" + LCTexturedModels.currentColor.getResourceSafeName()));
    }

    public static Identifier getWoodenAndColoredKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).withPath(LCTexturedModels.currentWood.generatePath("","/" + LCTexturedModels.currentColor.getResourceSafeName()));
    }

    public static class PrefixTemplate extends ModelTemplate {

        private final String prefix;
        private Function<Block,Identifier> blockKeyGetter = BuiltInRegistries.BLOCK::getKey;
        private Function<Item,Identifier> itemKeyGetter = BuiltInRegistries.ITEM::getKey;
        public PrefixTemplate(ModelTemplate template, String prefix) {
            super(template.model,template.suffix,template.requiredSlots.toArray(TextureSlot[]::new));
            this.prefix = prefix;
        }

        public PrefixTemplate withCustomBlockKey(Function<Block,Identifier> blockKeyGetter) { this.blockKeyGetter = blockKeyGetter; return this; }
        public PrefixTemplate withCustomItemKey(Function<Item,Identifier> itemKeyGetter) { this.itemKeyGetter = itemKeyGetter; return this; }

        private Identifier getBlockModelLocation(Block block, String suffix) {
            Identifier key = this.blockKeyGetter.apply(block);
            return key.withPath(path -> "block/" + this.prefix + path + suffix);
        }
        private Identifier getItemModelLocation(Item item, String suffix) {
            Identifier key = this.itemKeyGetter.apply(item);
            return key.withPath(path -> "item/" + this.prefix + path + suffix);
        }

        @Override
        public Identifier getDefaultModelLocation(Block block) {
            return this.getBlockModelLocation(block,this.suffix.orElse(""));
        }
        @Override
        public Identifier create(Block block, TextureMapping textures, BiConsumer<Identifier, ModelInstance> output) {
            return this.create(this.getDefaultModelLocation(block), textures, output);
        }
        @Override
        public Identifier createWithSuffix(Block block, String extraSuffix, TextureMapping textures, BiConsumer<Identifier, ModelInstance> output) {
            return this.create(this.getBlockModelLocation(block, extraSuffix + this.suffix.orElse("")), textures, output);
        }
        @Override
        public Identifier createWithOverride(Block block, String suffixOverride, TextureMapping textures, BiConsumer<Identifier, ModelInstance> output) {
            return this.create(this.getBlockModelLocation(block, suffixOverride), textures, output);
        }
        @Override
        public Identifier create(Item item, TextureMapping textures, BiConsumer<Identifier, ModelInstance> output) {
            return this.create(this.getItemModelLocation(item,this.suffix.orElse("")), textures, output);
        }

    }


}
