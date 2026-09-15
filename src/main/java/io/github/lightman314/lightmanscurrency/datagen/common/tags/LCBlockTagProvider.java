package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
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

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER)
                .add(LCBlocks.COIN_PILE_IRON,LCBlocks.COIN_BLOCK_IRON)
                .add(LCBlocks.COIN_PILE_GOLD,LCBlocks.COIN_BLOCK_GOLD)
                .add(LCBlocks.COIN_PILE_EMERALD,LCBlocks.COIN_BLOCK_EMERALD)
                .add(LCBlocks.COIN_PILE_DIAMOND,LCBlocks.COIN_BLOCK_DIAMOND)
                .add(LCBlocks.COIN_PILE_NETHERITE,LCBlocks.COIN_BLOCK_NETHERITE)
                .add(LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER)
                .add(LCBlocks.COIN_MINT)
                .addTag(LCTags.Blocks.GROUP_DISPLAY_CASE);

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(LCTags.Blocks.GROUP_CARD_DISPLAY);

        this.tag(LCTags.Blocks.MULTI_BLOCK);

        this.tag(LCTags.Blocks.OWNER_PROTECTED)
                .addTag(LCTags.Blocks.GROUP_DISPLAY_CASE)
                .addTag(LCTags.Blocks.GROUP_CARD_DISPLAY);

        this.tag(LCTags.Blocks.SAFE_INTERACTABLE)
                .addTag(LCTags.Blocks.OWNER_PROTECTED);

        //Trader Groups for convenience
        this.tag(LCTags.Blocks.GROUP_DISPLAY_CASE)
                .addBundle(LCBlocks.DISPLAY_CASE);
        this.tag(LCTags.Blocks.GROUP_CARD_DISPLAY)
                .addBundle(LCBlocks.CARD_DISPLAY);

    }

    @Override
    protected LCTagAppender<Block,Block> tag(TagKey<Block> tag) {
        return new LCTagAppender.Wrapper<>(super.tag(tag));
    }

}
