package io.github.lightman314.lightmanscurrency.api.helpers.keys;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record BlockItemTagKey(TagKey<Block> block,TagKey<Item> item) {
    public BlockItemTagKey(Identifier key) { this(TagKey.create(Registries.BLOCK,key),TagKey.create(Registries.ITEM,key)); }
}