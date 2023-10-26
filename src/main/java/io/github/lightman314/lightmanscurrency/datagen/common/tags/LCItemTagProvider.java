package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.IOptionalKey;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class LCItemTagProvider extends ItemTagsProvider {


    public LCItemTagProvider(DataGenerator gen, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, blockTagsProvider, LightmansCurrency.MODID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void addTags() {

        ///LIGHTMANS CURRENCY TAGS
        //Coin Tag
        this.cTag(LCTags.Items.COINS)
                .add(ModItems.COIN_COPPER)
                .add(ModItems.COIN_IRON)
                .add(ModItems.COIN_GOLD)
                .add(ModItems.COIN_EMERALD)
                .add(ModItems.COIN_DIAMOND)
                .add(ModItems.COIN_NETHERITE);

        //Coin Mint Material Tag for Coin Mint Recipe
        this.cTag(LCTags.Items.COIN_MINTING_MATERIAL)
                .addTag(Tags.Items.INGOTS_COPPER)
                .addTag(Tags.Items.INGOTS_IRON)
                .addTag(Tags.Items.INGOTS_GOLD)
                .addTag(Tags.Items.GEMS_EMERALD)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.INGOTS_NETHERITE);

        //Wallet Tag
        this.cTag(LCTags.Items.WALLET)
                .add(ModItems.WALLET_COPPER)
                .add(ModItems.WALLET_IRON)
                .add(ModItems.WALLET_GOLD)
                .add(ModItems.WALLET_EMERALD)
                .add(ModItems.WALLET_DIAMOND)
                .add(ModItems.WALLET_NETHERITE);

        //Trader Tags
        this.cTag(LCTags.Items.TRADER)
                .addTag(LCTags.Items.TRADER_NORMAL)
                .addTag(LCTags.Items.TRADER_SPECIALTY)
                .addTag(LCTags.Items.TRADER_NETWORK);

        //Normal
        this.cTag(LCTags.Items.TRADER_NORMAL)
                .addTag(LCTags.Items.TRADER_DISPLAY_CASE)
                .addTag(LCTags.Items.TRADER_VENDING_MACHINE)
                .addTag(LCTags.Items.TRADER_LARGE_VENDING_MACHINE)
                .addTag(LCTags.Items.TRADER_SHELF)
                .addTag(LCTags.Items.TRADER_SHELF_2x2)
                .addTag(LCTags.Items.TRADER_CARD_DISPLAY)
                .addTag(LCTags.Items.TRADER_FREEZER);

        //Normal Traders
        this.cTag(LCTags.Items.TRADER_DISPLAY_CASE).add(ModBlocks.DISPLAY_CASE);
        this.cTag(LCTags.Items.TRADER_VENDING_MACHINE).add(ModBlocks.VENDING_MACHINE);
        this.cTag(LCTags.Items.TRADER_LARGE_VENDING_MACHINE).add(ModBlocks.VENDING_MACHINE_LARGE);
        this.cTag(LCTags.Items.TRADER_SHELF).add(ModBlocks.SHELF);
        this.cTag(LCTags.Items.TRADER_SHELF_2x2).add(ModBlocks.SHELF_2x2);
        this.cTag(LCTags.Items.TRADER_CARD_DISPLAY).add(ModBlocks.CARD_DISPLAY);
        this.cTag(LCTags.Items.TRADER_FREEZER).add(ModBlocks.FREEZER);

        //Specialty
        this.cTag(LCTags.Items.TRADER_SPECIALTY)
                .addTag(LCTags.Items.TRADER_SPECIALTY_ARMOR_DISPLAY)
                .addTag(LCTags.Items.TRADER_SPECIALTY_PAYGATE)
                .addTag(LCTags.Items.TRADER_SPECIALTY_TICKET_KIOSK)
                .addTag(LCTags.Items.TRADER_SPECIALTY_BOOKSHELF)
                .addTag(LCTags.Items.TRADER_SPECIALTY_SLOT_MACHINE);

        //Specialty Traders
        this.cTag(LCTags.Items.TRADER_SPECIALTY_ARMOR_DISPLAY).add(ModBlocks.ARMOR_DISPLAY);
        this.cTag(LCTags.Items.TRADER_SPECIALTY_PAYGATE).add(ModBlocks.PAYGATE);
        this.cTag(LCTags.Items.TRADER_SPECIALTY_TICKET_KIOSK).add(ModBlocks.TICKET_KIOSK);
        this.cTag(LCTags.Items.TRADER_SPECIALTY_BOOKSHELF).add(ModBlocks.BOOKSHELF_TRADER);
        this.cTag(LCTags.Items.TRADER_SPECIALTY_SLOT_MACHINE).add(ModBlocks.SLOT_MACHINE);

        //Network
        this.cTag(LCTags.Items.TRADER_NETWORK).addTag(LCTags.Items.TRADER_NETWORK_ITEM);
        this.cTag(LCTags.Items.TRADER_NETWORK_ITEM)
                .add(ModBlocks.ITEM_NETWORK_TRADER_1)
                .add(ModBlocks.ITEM_NETWORK_TRADER_2)
                .add(ModBlocks.ITEM_NETWORK_TRADER_3)
                .add(ModBlocks.ITEM_NETWORK_TRADER_4);

        //Network Terminal
        this.cTag(LCTags.Items.NETWORK_TERMINAL)
                .add(ModBlocks.TERMINAL)
                .add(ModItems.PORTABLE_TERMINAL)
                .add(ModBlocks.GEM_TERMINAL)
                .add(ModItems.PORTABLE_GEM_TERMINAL);
        //Deprecated Trading Terminal Tag
        this.cTag(LCTags.Items.TRADING_TERMINAL).addTag(LCTags.Items.NETWORK_TERMINAL);

        //ATM
        this.cTag(LCTags.Items.ATM)
                .add(ModBlocks.ATM)
                .add(ModItems.PORTABLE_ATM);

        //Trader Interface
        this.cTag(LCTags.Items.TRADER_INTERFACE).add(ModBlocks.ITEM_TRADER_INTERFACE);

        //Coin Jars
        this.cTag(LCTags.Items.COIN_JAR_NORMAL)
                .add(ModBlocks.PIGGY_BANK)
                .add(ModBlocks.COINJAR_BLUE);
        this.cTag(LCTags.Items.COIN_JAR_ALL)
                .addTag(LCTags.Items.COIN_JAR_NORMAL)
                .add(ModBlocks.SUS_JAR);

        //Book Tag for Bookshelf Trader
        this.cTag(LCTags.Items.TRADABLE_BOOK)
                .add(Items.BOOK)
                .add(Items.ENCHANTED_BOOK)
                .add(Items.WRITABLE_BOOK)
                .add(Items.WRITTEN_BOOK);

        //Ticket Tags
        this.cTag(LCTags.Items.TICKET)
                .add(ModItems.TICKET)
                .add(ModItems.TICKET_PASS)
                .add(ModItems.TICKET_MASTER);
        this.cTag(LCTags.Items.TICKET_MATERIAL)
                .add(Items.PAPER)
                .addTag(LCTags.Items.TICKET)
                .add(ModItems.TICKET_STUB);

        ///VANILLA TAGS
        //Add non-copper coins to beacon payment items
        this.cTag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ModItems.COIN_IRON)
                .add(ModItems.COIN_GOLD)
                .add(ModItems.COIN_EMERALD)
                .add(ModItems.COIN_DIAMOND)
                .add(ModItems.COIN_NETHERITE);
        //Add gold items to piglin loved
        this.cTag(ItemTags.PIGLIN_LOVED)
                .add(ModItems.COIN_GOLD)
                .add(ModBlocks.COINPILE_GOLD)
                .add(ModBlocks.COINBLOCK_GOLD);

        ///MODDED TAGS
        //Add Wallets to Wallet Slot
        this.cTag(new ResourceLocation("curios","wallet")).addTag(LCTags.Items.WALLET);
        //Add Portable Terminals to Charm Slot
        this.cTag(new ResourceLocation("curios","charm"))
                .add(ModItems.PORTABLE_TERMINAL)
                .add(ModItems.PORTABLE_GEM_TERMINAL)
                .add(ModItems.PORTABLE_ATM);

    }

    private CustomTagAppender cTag(TagKey<Item> tag) { return new CustomTagAppender(this.tag(tag)); }
    private CustomTagAppender cTag(ResourceLocation tag) { return new CustomTagAppender(this.tag(ItemTags.create(tag))); }

    private record CustomTagAppender(TagsProvider.TagAppender<Item> appender) {

        public CustomTagAppender add(ItemLike item) { this.appender.add(item.asItem()); return this; }
        public CustomTagAppender add(RegistryObject<? extends ItemLike> item) { this.add(item.get()); return this; }
        public CustomTagAppender addOptional(RegistryObject<? extends ItemLike> item) { this.appender.addOptional(item.getId()); return this; }
        public CustomTagAppender add(RegistryObjectBundle<? extends ItemLike,?> bundle) {
            bundle.forEach((key,item) -> {
                if(key instanceof IOptionalKey ok)
                {
                    if(ok.isModded())
                        this.addOptional(item);
                    else
                        this.add(item);
                }
                else
                    this.add(item);
            });
            return this;
        }
        public CustomTagAppender add(RegistryObjectBiBundle<? extends ItemLike,?,?> bundle) {
            bundle.forEach((key1,key2,item) -> {
                if(key1 instanceof IOptionalKey ok1)
                {
                    if(ok1.isModded())
                        this.addOptional(item);
                    else if(key2 instanceof IOptionalKey ok2)
                    {
                        if(ok2.isModded())
                            this.addOptional(item);
                        else
                            this.add(item);
                    }
                }
                else if(key2 instanceof IOptionalKey ok2)
                {
                    if(ok2.isModded())
                        this.addOptional(item);
                    else
                        this.add(item);
                }
                else
                    this.add(item);
            });
            return this;
        }
        public CustomTagAppender addTag(TagKey<Item> tag) { this.appender.addTag(tag); return this; }

    }
}