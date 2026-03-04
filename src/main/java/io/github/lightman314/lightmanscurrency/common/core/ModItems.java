package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.*;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.data.SoundEntry;
import io.github.lightman314.lightmanscurrency.common.items.cards.ATMCardItem;
import io.github.lightman314.lightmanscurrency.common.items.cards.PrepaidCardItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModItems {

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(BuiltInRegistries.ITEM, LightmansCurrency.MODID);

    //Coins
	public static final Supplier<Item> COIN_COPPER = registerBasic("coin_copper");
	public static final Supplier<Item> COIN_IRON = registerBasic("coin_iron");
	public static final Supplier<Item> COIN_GOLD = registerBasic("coin_gold");
	public static final Supplier<Item> COIN_EMERALD = registerBasic("coin_emerald");
	public static final Supplier<Item> COIN_DIAMOND = registerBasic("coin_diamond");
	public static final Supplier<Item> COIN_NETHERITE = registerBasic("coin_netherite",Item.Properties::fireResistant);

    //Chocolate Coins
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_COPPER = register("coin_chocolate_copper",p -> new ChocolateCoinItem(p,1f));
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_IRON = register("coin_chocolate_iron", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.DIG_SPEED, 600)));
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_GOLD = register("coin_chocolate_gold",p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800)));
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_EMERALD = register("coin_chocolate_emerald", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.LUCK, 1000)));
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_DIAMOND = register("coin_chocolate_diamond", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200)));
	public static final Supplier<ChocolateCoinItem> COIN_CHOCOLATE_NETHERITE = register("coin_chocolate_netherite", p ->
            new ChocolateCoinItem(p.fireResistant(),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400),
            new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4),
            new MobEffectInstance(MobEffects.REGENERATION, 100, 1)));

    //Ancient Coin
	public static final Supplier<AncientCoinItem> COIN_ANCIENT = register("coin_ancient", p -> new AncientCoinItem(p.rarity(Rarity.RARE)));

    //Misc
	public static final Supplier<GachaBallItem> GACHA_BALL = register("gacha_ball",GachaBallItem::new);
	public static final Supplier<Item> TRADING_CORE = registerBasic("trading_core");

    //Tickets
	public static final Supplier<TicketItem> TICKET = register("ticket",TicketItem::new);
	public static final Supplier<TicketItem> TICKET_PASS = register("ticket_pass",TicketItem::new,p -> p.rarity(Rarity.UNCOMMON));
	public static final Supplier<TicketItem> TICKET_MASTER = register("master_ticket",TicketItem::new,p -> p.rarity(Rarity.RARE).stacksTo(1));
	public static final Supplier<CouponItem> COUPON = register("coupon",CouponItem::new);
	public static final Supplier<Item> TICKET_STUB = registerBasic("ticket_stub");

    //Golden Tickets
	public static final Supplier<Item> GOLDEN_TICKET = register("golden_ticket",TicketItem::new);
	public static final Supplier<Item> GOLDEN_TICKET_PASS = register("golden_ticket_pass",TicketItem::new,p -> p.rarity(Rarity.UNCOMMON));
	public static final Supplier<Item> GOLDEN_TICKET_MASTER = register("golden_master_ticket",TicketItem::new,p -> p.rarity(Rarity.RARE).stacksTo(1));
	public static final Supplier<Item> GOLDEN_TICKET_STUB = registerBasic("golden_ticket_stub");

    //Wallets
	public static final Supplier<WalletItem> WALLET_LEATHER = register("wallet_leather",p ->
            new WalletItem(6,WalletItem.lazyModel("wallet_leather"),false,0,0,p));
	public static final Supplier<WalletItem> WALLET_COPPER = register("wallet_copper", p ->
            new WalletItem(6,WalletItem.lazyModel("wallet_copper"),p));
	public static final Supplier<WalletItem> WALLET_IRON = register("wallet_iron",p ->
            new WalletItem(12,WalletItem.lazyModel("wallet_iron"),p));
	public static final Supplier<WalletItem> WALLET_GOLD = register("wallet_gold",p ->
            new WalletItem(18,WalletItem.lazyModel("wallet_gold"),p));
	public static final Supplier<WalletItem> WALLET_EMERALD = register("wallet_emerald",p ->
            new WalletItem(24,WalletItem.lazyModel("wallet_emerald"),p));
	public static final Supplier<WalletItem> WALLET_DIAMOND = register("wallet_diamond",p ->
            new WalletItem(30,WalletItem.lazyModel("wallet_diamond"),p));
	public static final Supplier<WalletItem> WALLET_NETHERITE = register("wallet_netherite",p ->
            new WalletItem(36,WalletItem.lazyModel("wallet_netherite"),p),
            p -> p.rarity(Rarity.RARE).fireResistant());
	public static final Supplier<WalletItem> WALLET_NETHER_STAR = register("wallet_nether_star",p ->
            new WalletItem(54,WalletItem.lazyModel("wallet_nether_star"),true,1,WalletItem.MAX_WALLET_SLOTS - 54,p),
            p -> p.rarity(Rarity.EPIC).fireResistant());
	public static final Supplier<WalletItem> WALLET_ENDER_DRAGON = register("wallet_ender_dragon",p ->
            new WalletItem(42, WalletItem.lazyModel("wallet_ender_dragon"), false, 3, WalletItem.MAX_WALLET_SLOTS - 42, SoundEntry.builder()
            .add(50,WalletItem.DEFAULT_COIN_COLLECT_SOUND)
            .addVanilla(25,"entity.ender_dragon.growl")
            .addVanilla(25,"entity.ender_dragon.ambient")
            .addVanilla(1,"entity.player.burp")
            .build(),p),
            p -> p.rarity(Rarity.EPIC).fireResistant());

    //Portable Block Items
	public static final Supplier<PortableTerminalItem> PORTABLE_TERMINAL = register("portable_terminal",PortableTerminalItem::new);
	public static final Supplier<PortableTerminalItem> PORTABLE_GEM_TERMINAL = register("portable_gem_terminal",PortableTerminalItem::new);
	public static final Supplier<PortableATMItem> PORTABLE_ATM = register("portable_atm",PortableATMItem::new);

    //Utility Items
    public static final Supplier<TransactionRegisterItem> TRANSACTION_REGISTER = register("transaction_register",TransactionRegisterItem::new);

    //Item Capacity Upgrades
	public static final Supplier<CapacityUpgradeItem> ITEM_CAPACITY_UPGRADE_1 = register("item_capacity_upgrade_1",p -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY,LCConfig.SERVER.itemCapacityUpgrade1,p));
	public static final Supplier<CapacityUpgradeItem> ITEM_CAPACITY_UPGRADE_2 = register("item_capacity_upgrade_2",p -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY,LCConfig.SERVER.itemCapacityUpgrade2,p));
	public static final Supplier<CapacityUpgradeItem> ITEM_CAPACITY_UPGRADE_3 = register("item_capacity_upgrade_3",p -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY,LCConfig.SERVER.itemCapacityUpgrade3,p));
	public static final Supplier<CapacityUpgradeItem> ITEM_CAPACITY_UPGRADE_4 = register("item_capacity_upgrade_4",p -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY,LCConfig.SERVER.itemCapacityUpgrade4,p));

    //Speed Upgrades
	public static final Supplier<SpeedUpgradeItem> SPEED_UPGRADE_1 = register("speed_upgrade_1",p -> new SpeedUpgradeItem(4,p));
	public static final Supplier<SpeedUpgradeItem> SPEED_UPGRADE_2 = register("speed_upgrade_2",p -> new SpeedUpgradeItem(8,p));
	public static final Supplier<SpeedUpgradeItem> SPEED_UPGRADE_3 = register("speed_upgrade_3",p -> new SpeedUpgradeItem(12,p));
	public static final Supplier<SpeedUpgradeItem> SPEED_UPGRADE_4 = register("speed_upgrade_4",p -> new SpeedUpgradeItem(16,p));
	public static final Supplier<SpeedUpgradeItem> SPEED_UPGRADE_5 = register("speed_upgrade_5",p -> new SpeedUpgradeItem(20,p));

    //Trade Offer Upgrades
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_1 = register("offer_upgrade_1",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,1,new Item.Properties()));
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_2 = register("offer_upgrade_2",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,2,new Item.Properties()));
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_3 = register("offer_upgrade_3",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,4,new Item.Properties()));
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_4 = register("offer_upgrade_4",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,8,new Item.Properties()));
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_5 = register("offer_upgrade_5",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,12,new Item.Properties()));
	public static final Supplier<CapacityUpgradeItem> OFFER_UPGRADE_6 = register("offer_upgrade_6",p -> new CapacityUpgradeItem(Upgrades.TRADE_OFFERS,16,new Item.Properties()));

    //Network Upgrade
	public static final Supplier<SimpleUpgradeItem> NETWORK_UPGRADE = register("network_upgrade",p -> new SimpleUpgradeItem(Upgrades.NETWORK,p));
	//Void Upgrade
    public static final Supplier<SimpleUpgradeItem> VOID_UPGRADE = register("void_upgrade",p -> new SimpleUpgradeItem(Upgrades.VOID,p));
    //Hopper Upgrade
	public static final Supplier<SimpleUpgradeItem> HOPPER_UPGRADE = register("hopper_upgrade",p -> new SimpleUpgradeItem(Upgrades.HOPPER,p));

    //Trader Interface Interaction Upgrades
	public static final Supplier<InteractionUpgradeItem> INTERACTION_UPGRADE_1 = register("interaction_upgrade_1",p -> new InteractionUpgradeItem(p,LCConfig.SERVER.interactionUpgrade1));
	public static final Supplier<InteractionUpgradeItem> INTERACTION_UPGRADE_2 = register("interaction_upgrade_2",p -> new InteractionUpgradeItem(p,LCConfig.SERVER.interactionUpgrade2));
	public static final Supplier<InteractionUpgradeItem> INTERACTION_UPGRADE_3 = register("interaction_upgrade_3",p -> new InteractionUpgradeItem(p,LCConfig.SERVER.interactionUpgrade3));

    //Coin Chest Upgrades
	public static final Supplier<SimpleUpgradeItem> COIN_CHEST_EXCHANGE_UPGRADE = register("coin_chest_exchange_upgrade",p -> new SimpleUpgradeItem(Upgrades.COIN_CHEST_EXCHANGE,p));
	public static final Supplier<MagnetUpgradeItem> COIN_CHEST_MAGNET_UPGRADE_1 = register("coin_chest_magnet_upgrade_1",p -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange1,p));
	public static final Supplier<MagnetUpgradeItem> COIN_CHEST_MAGNET_UPGRADE_2 = register("coin_chest_magnet_upgrade_2",p -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange2,p));
	public static final Supplier<MagnetUpgradeItem> COIN_CHEST_MAGNET_UPGRADE_3 = register("coin_chest_magnet_upgrade_3",p -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange3,p));
	public static final Supplier<MagnetUpgradeItem> COIN_CHEST_MAGNET_UPGRADE_4 = register("coin_chest_magnet_upgrade_4",p -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange4,p));
	public static final Supplier<SimpleUpgradeItem> COIN_CHEST_BANK_UPGRADE = register("coin_chest_bank_upgrade",p -> new SimpleUpgradeItem(Upgrades.COIN_CHEST_BANK,p));
	public static final Supplier<SimpleUpgradeItem> COIN_CHEST_SECURITY_UPGRADE = register("coin_chest_security_upgrade",p -> new SimpleUpgradeItem(Upgrades.COIN_CHEST_SECURITY,p));

    //Smithing Template
	public static final Supplier<SmithingTemplateItem> UPGRADE_SMITHING_TEMPLATE = register("upgrade_smithing_template",p -> new SmithingTemplateItem(LCText.TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO.getWithStyle(ChatFormatting.BLUE), LCText.TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS.getWithStyle(ChatFormatting.BLUE),LCText.TOOLTIP_SMITHING_TEMPLATE_DESCRIPTION.getWithStyle(ChatFormatting.GRAY),LCText.TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION.get(),LCText.TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION.get(),new ArrayList<>(),Lists.newArrayList(SmithingTemplateItem.EMPTY_SLOT_INGOT,SmithingTemplateItem.EMPTY_SLOT_EMERALD,SmithingTemplateItem.EMPTY_SLOT_DIAMOND,SmithingTemplateItem.EMPTY_SLOT_REDSTONE_DUST)));

    //Bank Cards
	public static final Supplier<ATMCardItem> ATM_CARD = register("atm_card",ATMCardItem::new,p -> p.stacksTo(1));
	public static final Supplier<PrepaidCardItem> PREPAID_CARD = register("prepaid_card",PrepaidCardItem::new,p -> p.stacksTo(1));

    //Variant Wand
	public static final Supplier<Item> VARIANT_WAND = registerBasic("variant_wand",p -> p.stacksTo(1));

    //Item Filter
	public static final Supplier<FilterItem> ITEM_TRADE_FILTER = register("item_trade_filter",FilterItem::new);

    //Easy Registration methods, future-proofed to work with 21.6 registration methods
    public static DeferredHolder<Item,Item> registerBasic(String id) { return register(id,Item::new); }
    public static DeferredHolder<Item,Item> registerBasic(String id, UnaryOperator<Item.Properties> propertyBuilder) { return register(id,Item::new,propertyBuilder); }
    public static <T extends Item> DeferredHolder<Item,T> register(String id, Function<Item.Properties,T> factory) { return register(id,factory,UnaryOperator.identity()); }
    public static <T extends Item> DeferredHolder<Item,T> register(String id, Function<Item.Properties,T> factory, UnaryOperator<Item.Properties> propertyBuilder) {
        return REGISTER.register(id,() -> factory.apply(propertyBuilder.apply(new Item.Properties())));
    }

    public static void addAlias(String oldName,String newName) { REGISTER.addAlias(LightmansCurrency.id(oldName),LightmansCurrency.id(newName)); }

}
