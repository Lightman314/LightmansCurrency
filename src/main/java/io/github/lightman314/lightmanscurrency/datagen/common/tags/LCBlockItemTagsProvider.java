package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.BlockItemTagKey;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class LCBlockItemTagsProvider {

    private final Function<BlockItemTagKey,LCTagAppender<Block,?>> tagGetter;
    public LCBlockItemTagsProvider(Function<BlockItemTagKey,LCTagAppender<Block,?>> tagGetter) {
        this.tagGetter = tagGetter;
    }

    public void run() {
        this.tag(LCTags.BlocksAndItems.GROUP_CARD_DISPLAY)
                .addBundle(LCBlocks.CARD_DISPLAY);
        this.tag(LCTags.BlocksAndItems.GROUP_DISPLAY_CASE)
                .addBundle(LCBlocks.DISPLAY_CASE);
        this.tag(LCTags.BlocksAndItems.GROUP_SINGLE_SHELF)
                .addBundle(LCBlocks.SINGLE_SHELF);
        this.tag(LCTags.BlocksAndItems.GROUP_DOUBLE_SHELF)
                .addBundle(LCBlocks.DOUBLE_SHELF);
        this.tag(LCTags.BlocksAndItems.GROUP_CARD_DISPLAY)
                .addBundle(LCBlocks.CARD_DISPLAY);
        this.tag(LCTags.BlocksAndItems.GROUP_VENDING_MACHINE)
                .addBundle(LCBlocks.VENDING_MACHINE);
        this.tag(LCTags.BlocksAndItems.GROUP_LARGE_VENDING_MACHINE)
                .addBundle(LCBlocks.LARGE_VENDING_MACHINE);
        this.tag(LCTags.BlocksAndItems.GROUP_ITEM_NETWORK_TRADERS)
                .addBundle(LCBlocks.ITEM_NETWORK_TRADER);
    }

    protected final LCTagAppender<Block,?> tag(BlockItemTagKey tagKey) { return this.tagGetter.apply(tagKey); }

}