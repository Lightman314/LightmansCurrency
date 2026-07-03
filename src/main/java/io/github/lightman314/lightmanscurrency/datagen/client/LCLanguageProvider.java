package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Function;
import java.util.function.Supplier;

public class LCLanguageProvider extends LanguageProvider {


    public LCLanguageProvider(PackOutput output) { this(output, LCApi.MODID,"en_us"); }
    protected LCLanguageProvider(PackOutput output, String modid, String locale) { super(output, modid, locale); }

    @Override
    protected void addTranslations() {

        //Creative Tabs
        this.text(LCText.Other.CREATIVE_GROUP_COINS,"Coins & Items");
        this.text(LCText.Other.CREATIVE_GROUP_TRADERS,"Traders");

        //Coins
        this.item(LCItems.COIN_COPPER,"Copper Coin");
        this.itemPlural(LCItems.COIN_COPPER,"Copper Coins");
        this.itemInitial(LCItems.COIN_COPPER,"c");
        this.item(LCItems.COIN_IRON,"Iron Coin");
        this.itemPlural(LCItems.COIN_IRON,"Iron Coins");
        this.itemInitial(LCItems.COIN_IRON,"i");
        this.item(LCItems.COIN_GOLD,"Gold Coin");
        this.itemPlural(LCItems.COIN_GOLD,"Gold Coins");
        this.itemInitial(LCItems.COIN_GOLD,"g");
        this.item(LCItems.COIN_EMERALD,"Emerald Coin");
        this.itemPlural(LCItems.COIN_EMERALD,"Emerald Coins");
        this.itemInitial(LCItems.COIN_EMERALD,"e");
        this.item(LCItems.COIN_DIAMOND,"Diamond Coin");
        this.itemPlural(LCItems.COIN_DIAMOND,"Diamond Coins");
        this.itemInitial(LCItems.COIN_DIAMOND,"d");
        this.item(LCItems.COIN_NETHERITE,"Netherite Coin");
        this.itemPlural(LCItems.COIN_NETHERITE,"Netherite Coins");
        this.itemInitial(LCItems.COIN_NETHERITE,"n");

        //Chocolate Coins
        this.item(LCItems.COIN_CHOCOLATE_COPPER,"Chocolate Copper Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_COPPER,"Chocolate Copper Coins");
        this.item(LCItems.COIN_CHOCOLATE_IRON,"Chocolate Iron Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_IRON,"Chocolate Iron Coins");
        this.item(LCItems.COIN_CHOCOLATE_GOLD,"Chocolate Gold Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_GOLD,"Chocolate Gold Coins");
        this.item(LCItems.COIN_CHOCOLATE_EMERALD,"Chocolate Emerald Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_EMERALD,"Chocolate Emerald Coins");
        this.item(LCItems.COIN_CHOCOLATE_DIAMOND,"Chocolate Diamond Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_DIAMOND,"Chocolate Diamond Coins");
        this.item(LCItems.COIN_CHOCOLATE_NETHERITE,"Chocolate Netherite Coin");
        this.itemPlural(LCItems.COIN_CHOCOLATE_NETHERITE,"Chocolate Netherite Coins");

        //Wallets
        this.item(LCItems.WALLET_COPPER,"Copper Wallet");
        this.item(LCItems.WALLET_IRON,"Iron Wallet");
        this.item(LCItems.WALLET_GOLD,"Gold Wallet");
        this.item(LCItems.WALLET_EMERALD,"Emerald Wallet");
        this.item(LCItems.WALLET_DIAMOND,"Diamond Wallet");
        this.item(LCItems.WALLET_NETHERITE,"Netherite Wallet");
        this.item(LCItems.WALLET_NETHER_STAR,"Nether Star Wallet");
        this.item(LCItems.WALLET_ENDER_DRAGON,"Ender Dragon Wallet");

        //Coin Piles
        this.block(LCBlocks.COIN_PILE_COPPER,"Pile of Copper Coins");
        this.blockPlural(LCBlocks.COIN_PILE_COPPER,"Piles of Copper Coins");
        this.block(LCBlocks.COIN_PILE_IRON,"Pile of Iron Coins");
        this.blockPlural(LCBlocks.COIN_PILE_IRON,"Piles of Iron Coins");
        this.block(LCBlocks.COIN_PILE_GOLD,"Pile of Gold Coins");
        this.blockPlural(LCBlocks.COIN_PILE_GOLD,"Piles of Gold Coins");
        this.block(LCBlocks.COIN_PILE_EMERALD,"Pile of Emerald Coins");
        this.blockPlural(LCBlocks.COIN_PILE_EMERALD,"Piles of Emerald Coins");
        this.block(LCBlocks.COIN_PILE_DIAMOND,"Pile of Diamond Coins");
        this.blockPlural(LCBlocks.COIN_PILE_DIAMOND,"Piles of Diamond Coins");
        this.block(LCBlocks.COIN_PILE_NETHERITE,"Pile of Netherite Coins");
        this.blockPlural(LCBlocks.COIN_PILE_NETHERITE,"Piles of Netherite Coins");

        //Coin Blocks
        this.block(LCBlocks.COIN_BLOCK_COPPER,"Block of Copper Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_COPPER,"Blocks of Copper Coins");
        this.block(LCBlocks.COIN_BLOCK_IRON,"Block of Iron Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_IRON,"Blocks of Iron Coins");
        this.block(LCBlocks.COIN_BLOCK_GOLD,"Block of Gold Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_GOLD,"Blocks of Gold Coins");
        this.block(LCBlocks.COIN_BLOCK_EMERALD,"Block of Emerald Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_EMERALD,"Blocks of Emerald Coins");
        this.block(LCBlocks.COIN_BLOCK_DIAMOND,"Block of Diamond Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_DIAMOND,"Blocks of Diamond Coins");
        this.block(LCBlocks.COIN_BLOCK_NETHERITE,"Block of Netherite Coins");
        this.blockPlural(LCBlocks.COIN_BLOCK_NETHERITE,"Blocks of Netherite Coins");

        //Chocolate Coin Piles


        //Display Cases
        this.coloredBlock(LCBlocks.DISPLAY_CASE,color -> color + " Display Case");


        //Wallet Tooltips
        this.text(LCText.Items.TOOLTIP_WALLET_CAPACITY,"Has %s coin slots");
        this.text(LCText.Items.TOOLTIP_WALLET_UPGRADEABLE,"Use a [%1$s] on this in your inventory to increase the wallets capacity by %2$s","Can by upgraded %3$s more times");
        this.text(LCText.Items.TOOLTIP_WALLET_STORED_MONEY,"Contains:");

        //Trader Tooltips
        this.text(LCText.Trader.TOOLTIP_TRADE_EDIT_TAB,"Edit Trades");
        this.text(LCText.Trader.TOOLTIP_ITEM_STORAGE,"Item Storage");

    }

    protected String getColorName(DyeColor color) { return EnumHelper.prettyName(color); }

    protected final void item(Supplier<? extends Item> item, String translation) { this.add(item.get(),translation); }
    protected final void itemPlural(Supplier<? extends Item> item,String translation) { this.add(item.get().getDescriptionId() + ".plural",translation); }
    protected final void itemInitial(Supplier<? extends Item> item,String translation) { this.add(item.get().getDescriptionId() + ".initial",translation); }
    protected final void block(Supplier<? extends Block> block, String translation) { this.add(block.get(),translation); }
    protected final void blockPlural(Supplier<? extends Block> block, String translation) { this.add(block.get().getDescriptionId() + ".plural",translation); }
    protected final void blockInitial(Supplier<? extends Block> block, String translation) { this.add(block.get().getDescriptionId() + ".initial",translation); }

    protected final void coloredBlock(DeferredHolderBundle<DyeColor,Block,? extends Block> bundle, Function<String,String> translation) {
        bundle.forEach((color,block) ->
            this.add(block,translation.apply(this.getColorName(color))));
    }

    protected final void text(TextEntry text,String translation) { this.add(text.getKey(),translation); }
    protected final void text(MultiLineTextEntry text,String... translations) {
        int index = 0;
        for(String line : translations)
            this.add(text.getKey(index++),line);
    }

}