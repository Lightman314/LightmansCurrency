package io.github.lightman314.lightmanscurrency.datagen.common.advancements;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.advancements.date.DateTrigger;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.text.AdvancementTextEntry;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.CustomProfessions;
import io.github.lightman314.lightmanscurrency.datagen.util.EmptyHolder;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LCCurrencyAdvancements implements AdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(@Nonnull HolderLookup.Provider lookup, @Nonnull Consumer<AdvancementHolder> consumer, @Nonnull ExistingFileHelper existingFileHelper) {
        AdvancementHolder root = save(consumer, Builder.advancement()
                        .display(ModItems.COIN_GOLD.get(), LCText.ADVANCEMENT_ROOT.titleText.get(), LCText.ADVANCEMENT_ROOT.descriptionText.get(), ResourceLocation.fromNamespaceAndPath("lightmanscurrency","textures/gui/advancements/backgrounds/currency.png"), AdvancementType.TASK,false,false,false)
                        .addCriterion("coin", ezItemTrigger(LCTags.Items.COINS)),
                "currency/root");
        AdvancementHolder mfp = save(consumer, Builder.advancement()
                        .parent(root)
                        .display(ezDisplay(ModItems.COIN_COPPER, LCText.ADVANCEMENT_MY_FIRST_PENNY))
                        .addCriterion("coin", ezItemTrigger(LCTags.Items.COINS)),
                "currency/myfirstpenny");
        //Trader Advancements
        AdvancementHolder tc = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModItems.TRADING_CORE, LCText.ADVANCEMENT_TRADING_CORE))
                        .addCriterion("trading_core",ezItemTrigger(ModItems.TRADING_CORE.get())),
                "currency/trading_core");
        AdvancementHolder trader = save(consumer,Builder.advancement()
                        .parent(tc)
                        .display(ezDisplay(ModBlocks.CARD_DISPLAY.get(WoodType.OAK, Color.RED),LCText.ADVANCEMENT_TRADER))
                        .addCriterion("trader",ezItemTrigger(LCTags.Items.TRADER))
                ,"currency/trader");
        AdvancementHolder specialTrader = save(consumer,Builder.advancement()
                        .parent(tc)
                        .display(ezDisplay(ModBlocks.ARMOR_DISPLAY,LCText.ADVANCEMENT_SPECIALTY_TRADER))
                        .addCriterion("specialty_trader",ezItemTrigger(LCTags.Items.TRADER_SPECIALTY))
                ,"currency/specialty_trader");
        AdvancementHolder networkTrader = save(consumer,Builder.advancement()
                        .parent(tc)
                        .display(ezDisplay(ModBlocks.ITEM_NETWORK_TRADER_4, LCText.ADVANCEMENT_NETWORK_TRADER))
                        .addCriterion("network_trader", ezItemTrigger(LCTags.Items.TRADER_NETWORK))
                ,"currency/network_trader");
        AdvancementHolder traderInterface = save(consumer,Builder.advancement()
                        .parent(networkTrader)
                        .display(ezDisplay(ModBlocks.ITEM_TRADER_INTERFACE,LCText.ADVANCEMENT_TRADER_INTERFACE))
                        .addCriterion("trader_interface",ezItemTrigger(LCTags.Items.TRADER_INTERFACE))
                ,"currency/trader_interface");
        //Terminal
        AdvancementHolder terminal = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModBlocks.TERMINAL,LCText.ADVANCEMENT_TERMINAL))
                        .addCriterion("terminal", ezItemTrigger(LCTags.Items.NETWORK_TERMINAL))
                ,"currency/terminal");
        //ATM
        AdvancementHolder atm = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModBlocks.ATM,LCText.ADVANCEMENT_ATM))
                        .addCriterion("atm_block",ezItemTrigger(LCTags.Items.ATM))
                ,"currency/atm");
        AdvancementHolder bankerVillager = save(consumer,Builder.advancement()
                        .parent(atm)
                        .display(ezDisplay(ModBlocks.COINBLOCK_NETHERITE,LCText.ADVANCEMENT_BANKER_TRADE,AdvancementType.GOAL,true,true,false))
                        .addCriterion("banker_trade",ezVillagerTrigger(CustomProfessions.BANKER))
                ,"currency/banker_trade");
        //Coin Mint
        AdvancementHolder coinMint = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModBlocks.COIN_MINT,LCText.ADVANCEMENT_COIN_MINT))
                        .addCriterion("coin_mint", ezItemTrigger(ModBlocks.COIN_MINT.get()))
                ,"currency/coin_mint");
        //Wallet
        AdvancementHolder wallet = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModItems.WALLET_COPPER,LCText.ADVANCEMENT_WALLET_CRAFTING))
                        .addCriterion("wallet", ezItemTrigger(LCTags.Items.WALLET))
                ,"currency/wallet_crafting");
        AdvancementHolder netheriteWallet = save(consumer,Builder.advancement()
                        .parent(wallet)
                        .display(ezDisplay(ModItems.WALLET_NETHERITE,LCText.ADVANCEMENT_NETHERITE_WALLET))
                        .addCriterion("netherite_wallet", ezItemTrigger(ModItems.WALLET_NETHERITE.get()))
                ,"currency/netherite_wallet");
        AdvancementHolder netherStarWallet = save(consumer,Builder.advancement()
                .parent(netheriteWallet)
                .display(ezDisplay(ModItems.WALLET_NETHER_STAR,LCText.ADVANCEMENT_NETHER_STAR_WALLET))
                .addCriterion("nether_star_wallet", ezItemTrigger(ModItems.WALLET_NETHER_STAR.get())),
                "currency/nether_star_wallet");


        ItemStack enchantedWalletItem = new ItemStack(ModItems.WALLET_NETHERITE.get());
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        Holder<Enchantment> coinMagnetHolder = new EmptyHolder<>(ModEnchantments.COIN_MAGNET);
        enchantments.set(coinMagnetHolder,3);
        EnchantmentHelper.setEnchantments(enchantedWalletItem, enchantments.toImmutable());
        AdvancementHolder enchantedWallet = save(consumer,Builder.advancement()
                        .parent(wallet)
                        .display(ezDisplay(enchantedWalletItem,LCText.ADVANCEMENT_ENCHANTED_WALLET,AdvancementType.GOAL,true,true,false))
                        .addCriterion("enchanted_wallet",ezItemTrigger(b -> b.of(LCTags.Items.WALLET).withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(ImmutableList.of(new EnchantmentPredicate(coinMagnetHolder, MinMaxBounds.Ints.atLeast(1)))))))
                ,"currency/enchanted_wallet");//*/
        //Cash Register
        AdvancementHolder cashRegister = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModBlocks.CASH_REGISTER,LCText.ADVANCEMENT_CASH_REGISTER))
                        .addCriterion("cash_register",ezItemTrigger(ModBlocks.CASH_REGISTER.get()))
                ,"currency/cash_register");
        AdvancementHolder cashierVilager = save(consumer,Builder.advancement()
                        .parent(cashRegister)
                        .display(ezDisplay(Items.ENCHANTED_BOOK,LCText.ADVANCEMENT_CASHIER_TRADE,AdvancementType.GOAL,true,true,false))
                        .addCriterion("cashier_trade",ezVillagerTrigger(CustomProfessions.CASHIER))
                ,"currency/cashier_trade");
        //Sus Jar
        AdvancementHolder susJar = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ModBlocks.SUS_JAR.get(), LCText.ADVANCEMENT_JAR_OF_SUS.titleText.get(),LCText.ADVANCEMENT_JAR_OF_SUS.descriptionText.getWithStyle(ChatFormatting.BOLD),null,AdvancementType.TASK, true, true, true)
                        .addCriterion("jar_of_sus", ezItemTrigger(ModBlocks.SUS_JAR.get()))
                ,"currency/jar_of_sus");


        //Ancient Coins
        AdvancementHolder ancientCoins = save(consumer,Builder.advancement()
                .parent(mfp)
                .display(ezDisplay(AncientCoinType.GOLD.asItem(),LCText.ADVANCEMENT_ANCIENT_COIN, AdvancementType.GOAL, true, true, true))
                .addCriterion("ancient_coin", ezItemTrigger(ModItems.COIN_ANCIENT.get()))
                .rewards(AdvancementRewards.Builder.function(VersionUtil.lcResource("unlock_ancient_coins"))),
                "currency/ancient_coin");

        //Events
        AdvancementHolder chocolateCoins = save(consumer,Builder.advancement()
                        .parent(mfp)
                        .display(ezDisplay(ModItems.COIN_CHOCOLATE_COPPER,LCText.ADVANCEMENT_EVENT_CHOCOLATE, AdvancementType.CHALLENGE,true,false,true))
                        .addCriterion("has_coins",ezItemTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE))
                        .rewards(AdvancementRewards.Builder.function(VersionUtil.lcResource("events/unlock_chocolate")))
                ,"currency/events/chocolate_coins");
        AdvancementHolder christmas = save(consumer,Builder.advancement()
                        .parent(chocolateCoins)
                        .display(ezDisplay(ModItems.COIN_CHOCOLATE_GOLD,LCText.ADVANCEMENT_EVENT_CHRISTMAS,AdvancementType.TASK,true))
                        .addCriterion("event", ezEventTrigger(12,1,12,31))
                        .rewards(AdvancementRewards.Builder.function(VersionUtil.lcResource("events/gift_chocolate")))
                ,"currency/events/christmas");
        AdvancementHolder valentines = save(consumer,Builder.advancement()
                        .parent(chocolateCoins)
                        .display(ezDisplay(ModItems.COIN_CHOCOLATE_DIAMOND,LCText.ADVANCEMENT_EVENT_VALENTINES,AdvancementType.TASK,true))
                        .addCriterion("event", ezEventTrigger(2,13,2,15))
                        .rewards(AdvancementRewards.Builder.function(VersionUtil.lcResource("events/gift_chocolate")))
                ,"currency/events/valentines");
    }

    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull String translation){ return ezDisplay(icon, translation, AdvancementType.TASK, false); }
    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull AdvancementTextEntry translation){ return ezDisplay(icon, translation, AdvancementType.TASK, false); }

    private static DisplayInfo ezDisplay(@Nonnull ItemLike icon, @Nonnull String translation){ return ezDisplay(icon, translation, AdvancementType.TASK, true, true, false); }
    private static DisplayInfo ezDisplay(@Nonnull ItemLike icon, @Nonnull AdvancementTextEntry translation){ return ezDisplay(icon, translation, AdvancementType.TASK, true, true, false); }

    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull String translation, @Nonnull AdvancementType frame, boolean hidden){ return ezDisplay(icon, translation, AdvancementType.TASK, true, true, hidden); }
    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull AdvancementTextEntry translation, @Nonnull AdvancementType frame, boolean hidden){ return ezDisplay(icon, translation, AdvancementType.TASK, true, true, hidden); }

    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull String translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){ return ezDisplay(icon.get(),translation,frame,toast,chat,hidden); }
    private static DisplayInfo ezDisplay(@Nonnull Supplier<? extends ItemLike> icon, @Nonnull AdvancementTextEntry translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){ return ezDisplay(icon.get(),translation,frame,toast,chat,hidden); }

    private static DisplayInfo ezDisplay(@Nonnull ItemLike icon, @Nonnull String translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){ return ezDisplay(new ItemStack(icon),translation,frame,toast,chat,hidden); }
    private static DisplayInfo ezDisplay(@Nonnull ItemLike icon, @Nonnull AdvancementTextEntry translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){ return ezDisplay(new ItemStack(icon),translation,frame,toast,chat,hidden); }

    private static DisplayInfo ezDisplay(@Nonnull ItemStack icon, @Nonnull AdvancementTextEntry translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){
        return new DisplayInfo(icon, translation.titleText.get(),translation.descriptionText.get(), Optional.empty(), frame, toast, chat, hidden);
    }
    private static DisplayInfo ezDisplay(@Nonnull ItemStack icon, @Nonnull String translation, @Nonnull AdvancementType frame, boolean toast, boolean chat, boolean hidden){
        return new DisplayInfo(icon, Component.translatable(translation + ".title"), Component.translatable(translation + ".description"), Optional.empty(), frame, toast, chat, hidden);
    }

    private static Criterion<?> ezItemTrigger(@Nonnull TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }

    private static Criterion<?> ezItemTrigger(@Nonnull ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    private static Criterion<?> ezItemTrigger(@Nonnull Consumer<ItemPredicate.Builder> consumer) {
        ItemPredicate.Builder builder = ItemPredicate.Builder.item();
        consumer.accept(builder);
        return InventoryChangeTrigger.TriggerInstance.hasItems(builder.build());
    }

    private static Criterion<?> ezVillagerTrigger(@Nonnull Supplier<VillagerProfession> profession) { return ezVillagerTrigger(profession.get()); }
    private static Criterion<?> ezVillagerTrigger(@Nonnull VillagerProfession profession) {
        EntityPredicate.Builder builder = EntityPredicate.Builder.entity();
        CompoundTag requiredTag = new CompoundTag();
        CompoundTag vilagerData = new CompoundTag();
        vilagerData.putString("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString());
        requiredTag.put("VillagerData",vilagerData);
        builder.nbt(new NbtPredicate(requiredTag));
        return TradeTrigger.TriggerInstance.tradedWithVillager(builder);
    }

    private static Criterion<?> ezEventTrigger(int startMonth, int startDay, int endMonth, int endDay) {
        return DateTrigger.ofRange(startMonth,startDay,endMonth,endDay);
    }

    private AdvancementHolder save(@Nonnull Consumer<AdvancementHolder> consumer, @Nonnull Advancement.Builder builder, @Nonnull String id) {
        AdvancementHolder a = builder.build(VersionUtil.lcResource(id));
        consumer.accept(a);
        return a;
    }
}
