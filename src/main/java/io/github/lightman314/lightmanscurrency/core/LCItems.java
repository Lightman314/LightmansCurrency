package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeHolder;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.ConfigNumberSource;
import io.github.lightman314.lightmanscurrency.client.features.atm.PortableATMItem;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import io.github.lightman314.lightmanscurrency.features.chocolate_coins.ChocolateCoinItem;
import io.github.lightman314.lightmanscurrency.features.network_terminal.PortableTerminalItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletUpgradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class LCItems {

    private LCItems() {}

    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(LCApi.MODID);

    //Coins
    public static final DeferredItem<Item> COIN_COPPER = registerBasic("coin_copper");
    public static final DeferredItem<Item> COIN_IRON = registerBasic("coin_iron");
    public static final DeferredItem<Item> COIN_GOLD = registerBasic("coin_gold");
    public static final DeferredItem<Item> COIN_EMERALD = registerBasic("coin_emerald");
    public static final DeferredItem<Item> COIN_DIAMOND = registerBasic("coin_diamond");
    public static final DeferredItem<Item> COIN_NETHERITE = registerBasic("coin_netherite",Item.Properties::fireResistant);

    //Chocolate Coins
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_COPPER = register("chocolate_coin_copper",p -> new ChocolateCoinItem(p,1f));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_IRON = register("chocolate_coin_iron", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.HASTE, 600)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_GOLD = register("chocolate_coin_gold",p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.SPEED, 800)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_EMERALD = register("chocolate_coin_emerald", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.LUCK, 1000)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_DIAMOND = register("chocolate_coin_diamond", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.RESISTANCE, 1200)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_NETHERITE = register("chocolate_coin_netherite", p ->
            new ChocolateCoinItem(p.fireResistant(),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400),
                    new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4),
                    new MobEffectInstance(MobEffects.REGENERATION, 100, 1)));

    //Wallets
    public static final DeferredItem<WalletItem> WALLET_COPPER = registerWallet("wallet_copper",6);
    public static final DeferredItem<WalletItem> WALLET_IRON = registerWallet("wallet_iron",12);
    public static final DeferredItem<WalletItem> WALLET_GOLD = registerWallet("wallet_gold",18);
    public static final DeferredItem<WalletItem> WALLET_EMERALD = registerWallet("wallet_emerald",24);
    public static final DeferredItem<WalletItem> WALLET_DIAMOND = registerWallet("wallet_diamond",30);
    public static final DeferredItem<WalletItem> WALLET_NETHERITE = registerWallet("wallet_netherite",36, Item.Properties::fireResistant);
    public static final DeferredItem<WalletItem> WALLET_NETHER_STAR = registerWallet("wallet_nether_star",54,p ->
            p.fireResistant()
                    .component(LCDataComponents.WALLET_MAGNET_BONUS,1)
                    .component(LCDataComponents.WALLET_INVULNERABLE,Unit.INSTANCE));
    public static final DeferredItem<WalletItem> WALLET_ENDER_DRAGON = registerWallet("wallet_ender_dragon",36,p ->
            p.fireResistant() //TODO add dragon sounds
                    .component(LCDataComponents.WALLET_MAGNET_BONUS,3)
                    .component(LCDataComponents.WALLET_PICKUP_SOUND,new WeightedList.Builder<Identifier>()
                            .add(WalletItem.DEFAULT_SOUND,51)
                            .add(Identifier.withDefaultNamespace("entity.ender_dragon.growl"),25)
                            .add(Identifier.withDefaultNamespace("entity.ender_dragon.ambient"),25)
                            .add(Identifier.withDefaultNamespace("entity.player.burp"),1)
                            .build()
                    ));


    //Trading Core
    public static final DeferredItem<Item> TRADING_CORE = registerBasic("trading_core");
    public static final DeferredItem<SmithingTemplateItem> UPGRADE_SMITHING_TEMPLATE = register("upgrade_smithing_template",p -> new SmithingTemplateItem(
            LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO.getWithStyle(ChatFormatting.BLUE),
            LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS.getWithStyle(ChatFormatting.BLUE),
            LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION.get(),
            LCText.Resources.TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION.get(),
            new ArrayList<>(),
            List.of(SmithingTemplateItem.EMPTY_SLOT_INGOT,SmithingTemplateItem.EMPTY_SLOT_EMERALD,SmithingTemplateItem.EMPTY_SLOT_DIAMOND,SmithingTemplateItem.EMPTY_SLOT_REDSTONE_DUST),
            p.rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<PortableATMItem> ATM_PORTABLE = register("atm_portable",PortableATMItem::new);
    public static final DeferredItem<PortableTerminalItem> TRADING_TERMINAL_PORTABLE = register("trading_terminal_portable",PortableTerminalItem::new);

    //Upgrades
    public static final DeferredItem<Item> ITEM_CAPACITY_UPGRADE_1 = registerBasic("item_capacity_upgrade_1",
            CapacityUpgradeType.buildProperties(LCUpgrades.ITEM_CAPACITY,
                    () -> new ConfigNumberSource(LCConfig.SERVER.itemCapacityUpgrade1)));
    public static final DeferredItem<Item> ITEM_CAPACITY_UPGRADE_2 = registerBasic("item_capacity_upgrade_2",
            CapacityUpgradeType.buildProperties(LCUpgrades.ITEM_CAPACITY,
                    () -> new ConfigNumberSource(LCConfig.SERVER.itemCapacityUpgrade2)));
    public static final DeferredItem<Item> ITEM_CAPACITY_UPGRADE_3 = registerBasic("item_capacity_upgrade_3",
            CapacityUpgradeType.buildProperties(LCUpgrades.ITEM_CAPACITY,
                    () -> new ConfigNumberSource(LCConfig.SERVER.itemCapacityUpgrade3)));
    public static final DeferredItem<Item> ITEM_CAPACITY_UPGRADE_4 = registerBasic("item_capacity_upgrade_4",
            CapacityUpgradeType.buildProperties(LCUpgrades.ITEM_CAPACITY,
                    () -> new ConfigNumberSource(LCConfig.SERVER.itemCapacityUpgrade4)));

    public static final DeferredItem<Item> NETWORK_UPGRADE = registerBasic("network_upgrade",
            p -> p.component(LCDataComponents.UPGRADE_TYPE,new UpgradeHolder(LCUpgrades.NETWORK)));

    public static DeferredItem<Item> registerBasic(String id) { return register(id,Item::new); }
    public static DeferredItem<Item> registerBasic(String id,UnaryOperator<Item.Properties> propertyBuilder) { return register(id,Item::new,propertyBuilder); }

    public static DeferredItem<WalletItem> registerWallet(String id,int capacity) { return registerWallet(id,capacity,UnaryOperator.identity()); }
    public static DeferredItem<WalletItem> registerWallet(String id,int capacity,UnaryOperator<Item.Properties> propertyBuilder) { return registerWallet(id,(model,p) -> new WalletItem(p,model,capacity,WalletUpgradeData.DEFAULT),propertyBuilder); }
    public static DeferredItem<WalletItem> registerWallet(String id, BiFunction<Identifier,Item.Properties,WalletItem> factory) { return registerWallet(id,factory,UnaryOperator.identity()); }
    public static DeferredItem<WalletItem> registerWallet(String id, BiFunction<Identifier,Item.Properties,WalletItem> factory,UnaryOperator<Item.Properties> propertyBuilder) {
        return register(id,p -> factory.apply(WalletItem.model(id),p),propertyBuilder);
    }

    public static <T extends Item> DeferredItem<T> register(String id, Function<Item.Properties,T> factory) { return register(id,factory,UnaryOperator.identity()); }
    public static <T extends Item> DeferredItem<T> register(String id, Function<Item.Properties,T> factory, UnaryOperator<Item.Properties> propertyBuilder) {
        return REGISTER.registerItem(id,factory,propertyBuilder);
    }

    public static void addAlias(String oldName,String newName) { REGISTER.addAlias(LCApi.id(oldName),LCApi.id(newName)); }

}