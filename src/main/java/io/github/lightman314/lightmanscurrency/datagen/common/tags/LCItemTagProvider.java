package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;

import java.util.concurrent.CompletableFuture;

public class LCItemTagProvider extends ItemTagsProvider {

    public LCItemTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider) { this(output,lookupProvider,LCApi.MODID); }
    public LCItemTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookupProvider,String modid) {
        super(output,lookupProvider,modid);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        //Dyed Tags
        for(VanillaColor color : VanillaColor.values()) {
            this.ezTag(color.getDyedTag())
                    .addBundlePart2(LCBlocks.CARD_DISPLAY,color)
                    .add(LCBlocks.VENDING_MACHINE.get(color))
                    .add(LCBlocks.LARGE_VENDING_MACHINE.get(color));
        }

        //Money Tags
        this.ezTag(LCTags.Items.MONEY)
                .addTag(LCTags.Items.MONEY_COINS)
                .addTag(LCTags.Items.MONEY_EMERALDS)
                .addTag(LCTags.Items.MONEY_CHOCOLATE_COINS);

        this.ezTag(LCTags.Items.MONEY_COINS)
                .add(LCItems.COIN_COPPER)
                .add(LCItems.COIN_IRON)
                .add(LCItems.COIN_GOLD)
                .add(LCItems.COIN_EMERALD)
                .add(LCItems.COIN_DIAMOND)
                .add(LCItems.COIN_NETHERITE);

        this.ezTag(LCTags.Items.MONEY_EMERALDS)
                .add(Items.EMERALD)
                .add(Items.EMERALD_BLOCK);

        this.ezTag(LCTags.Items.MONEY_CHOCOLATE_COINS)
                .add(LCItems.COIN_CHOCOLATE_COPPER)
                .add(LCItems.COIN_CHOCOLATE_IRON)
                .add(LCItems.COIN_CHOCOLATE_GOLD)
                .add(LCItems.COIN_CHOCOLATE_EMERALD)
                .add(LCItems.COIN_CHOCOLATE_DIAMOND)
                .add(LCItems.COIN_CHOCOLATE_NETHERITE);

        this.ezTag(LCTags.Items.COIN_MINTING_MATERIAL)
                .addTag(Tags.Items.INGOTS_COPPER)
                .addTag(Tags.Items.INGOTS_IRON)
                .addTag(Tags.Items.INGOTS_GOLD)
                .addTag(Tags.Items.GEMS_EMERALD)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.INGOTS_NETHERITE);

        this.ezTag(LCTags.Items.WALLET)
                .add(LCItems.WALLET_COPPER)
                .add(LCItems.WALLET_IRON)
                .add(LCItems.WALLET_GOLD)
                .add(LCItems.WALLET_EMERALD)
                .add(LCItems.WALLET_DIAMOND)
                .add(LCItems.WALLET_NETHERITE)
                .add(LCItems.WALLET_NETHER_STAR)
                .add(LCItems.WALLET_ENDER_DRAGON);

        this.ezTag(LCTags.Items.WALLET_UPGRADE_MATERIAL)
                .add(Items.DIAMOND);

        this.ezTag(LCTags.Items.SETTINGS_READABLE)
                .add(Items.PAPER)
                .add(Items.WRITTEN_BOOK);
        this.ezTag(LCTags.Items.SETTINGS_WRITABLE)
                .add(Items.PAPER)
                .add(Items.BOOK)
                .add(Items.WRITABLE_BOOK)
                .add(Items.WRITTEN_BOOK);

        this.ezTag(LCTags.Items.ATM)
                .add(LCBlocks.ATM)
                .add(LCItems.ATM_PORTABLE);

        this.ezTag(LCTags.Items.NETWORK_TERMINAL)
                .add(LCBlocks.TRADING_TERMINAL)
                .add(LCItems.TRADING_TERMINAL_PORTABLE);

        this.ezTag(LCTags.Items.TRADERS)
                .addTag(LCTags.Items.TRADERS_ITEM)
                .addTag(LCTags.Items.TRADERS_NETWORK);

        this.ezTag(LCTags.Items.TRADERS_ITEM)
                .addTag(LCTags.Items.GROUP_DISPLAY_CASE)
                .addTag(LCTags.Items.GROUP_SINGLE_SHELF)
                .addTag(LCTags.Items.GROUP_DOUBLE_SHELF)
                .addTag(LCTags.Items.GROUP_CARD_DISPLAY)
                .addTag(LCTags.Items.GROUP_VENDING_MACHINE)
                .addTag(LCTags.Items.GROUP_LARGE_VENDING_MACHINE)
                .addTag(LCTags.Items.GROUP_ITEM_NETWORK_TRADERS);

        this.tag(LCTags.Items.TRADERS_NETWORK)
                .addTag(LCTags.Items.GROUP_ITEM_NETWORK_TRADERS);

        //Item Groups for convenience
        this.ezTag(LCTags.Items.GROUP_DISPLAY_CASE)
                .addBundle(LCBlocks.DISPLAY_CASE);
        this.ezTag(LCTags.Items.GROUP_SINGLE_SHELF)
                .addBundle(LCBlocks.SINGLE_SHELF);
        this.ezTag(LCTags.Items.GROUP_DOUBLE_SHELF)
                .addBundle(LCBlocks.DOUBLE_SHELF);
        this.ezTag(LCTags.Items.GROUP_CARD_DISPLAY)
                .addBundle(LCBlocks.CARD_DISPLAY);
        this.ezTag(LCTags.Items.GROUP_VENDING_MACHINE)
                .addBundle(LCBlocks.VENDING_MACHINE);
        this.ezTag(LCTags.Items.GROUP_LARGE_VENDING_MACHINE)
                .addBundle(LCBlocks.LARGE_VENDING_MACHINE);
        this.ezTag(LCTags.Items.GROUP_ITEM_NETWORK_TRADERS)
                .addBundle(LCBlocks.ITEM_NETWORK_TRADER);

        //Curios Tags
        this.tag(Identifier.fromNamespaceAndPath("curios",LCCuriosHelper.WALLET_SLOT))
                .addTag(LCTags.Items.WALLET);

    }

    @Override
    protected LCTagAppender<Item,Item> tag(TagKey<Item> tag) {
        return new LCTagAppender.Wrapper<>(super.tag(tag));
    }

    protected LCTagAppender<ItemLike,Item> ezTag(TagKey<Item> tag) { return this.tag(tag).map(ItemLike::asItem); }

    protected LCTagAppender<Item,Item> tag(Identifier tag) { return this.tag(TagKey.create(this.registryKey,tag)); }

}