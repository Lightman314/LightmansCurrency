package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.BlockItemTagKey;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class LCBlockTagProvider extends BlockTagsProvider {

    public LCBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) { this(output,lookupProvider, LCApi.MODID); }
    public LCBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) { super(output, lookupProvider, modId); }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        //Block And Item Tags
        new LCBlockItemTagsProvider(this::blockItemTag).run();

        //Vanilla Tags
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER)
                .add(LCBlocks.COIN_PILE_IRON,LCBlocks.COIN_BLOCK_IRON)
                .add(LCBlocks.COIN_PILE_GOLD,LCBlocks.COIN_BLOCK_GOLD)
                .add(LCBlocks.COIN_PILE_EMERALD,LCBlocks.COIN_BLOCK_EMERALD)
                .add(LCBlocks.COIN_PILE_DIAMOND,LCBlocks.COIN_BLOCK_DIAMOND)
                .add(LCBlocks.COIN_PILE_NETHERITE,LCBlocks.COIN_BLOCK_NETHERITE)
                .add(LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER)
                .add(LCBlocks.COIN_MINT)
                .add(LCBlocks.TRADING_TERMINAL)
                .addTag(LCTags.BlocksAndItems.GROUP_DISPLAY_CASE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_VENDING_MACHINE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_LARGE_VENDING_MACHINE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_ITEM_NETWORK_TRADERS.block());

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(LCTags.BlocksAndItems.GROUP_CARD_DISPLAY.block())
                .addTag(LCTags.BlocksAndItems.GROUP_SINGLE_SHELF.block())
                .addTag(LCTags.BlocksAndItems.GROUP_DOUBLE_SHELF.block());

        //Protection Tags
        this.tag(BlockTags.DRAGON_IMMUNE)
                .addTag(LCTags.Blocks.OWNER_PROTECTED);
        this.tag(BlockTags.WITHER_IMMUNE)
                .addTag(LCTags.Blocks.OWNER_PROTECTED);

        //LC Tags
        this.tag(LCTags.Blocks.MULTI_BLOCK)
                .addBundle(LCBlocks.VENDING_MACHINE)
                .addBundle(LCBlocks.LARGE_VENDING_MACHINE)
                .add(LCBlocks.ATM);

        this.tag(LCTags.Blocks.OWNER_PROTECTED)
                .addTag(LCTags.BlocksAndItems.GROUP_DISPLAY_CASE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_SINGLE_SHELF.block())
                .addTag(LCTags.BlocksAndItems.GROUP_DOUBLE_SHELF.block())
                .addTag(LCTags.BlocksAndItems.GROUP_CARD_DISPLAY.block())
                .addTag(LCTags.BlocksAndItems.GROUP_VENDING_MACHINE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_LARGE_VENDING_MACHINE.block())
                .addTag(LCTags.BlocksAndItems.GROUP_ITEM_NETWORK_TRADERS.block());

        this.tag(LCTags.Blocks.SAFE_INTERACTABLE)
                .add(LCBlocks.TRADING_TERMINAL)
                .addTag(LCTags.Blocks.OWNER_PROTECTED);

    }

    @Override
    protected LCTagAppender<Block,Block> tag(TagKey<Block> tag) {
        return new LCTagAppender.Wrapper<>(super.tag(tag));
    }

    protected LCTagAppender<Block,Block> blockItemTag(BlockItemTagKey tag) { return this.tag(tag.block()); }

}
