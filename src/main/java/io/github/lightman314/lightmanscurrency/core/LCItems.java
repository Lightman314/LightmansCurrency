package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.chocolate_coins.ChocolateCoinItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletUpgradeData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_COPPER = register("coin_chocolate_copper",p -> new ChocolateCoinItem(p,1f));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_IRON = register("coin_chocolate_iron", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.HASTE, 600)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_GOLD = register("coin_chocolate_gold",p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.SPEED, 800)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_EMERALD = register("coin_chocolate_emerald", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.LUCK, 1000)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_DIAMOND = register("coin_chocolate_diamond", p ->
            new ChocolateCoinItem(p,new MobEffectInstance(MobEffects.RESISTANCE, 1200)));
    public static final DeferredItem<ChocolateCoinItem> COIN_CHOCOLATE_NETHERITE = register("coin_chocolate_netherite", p ->
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
                    .component(LCDataComponents.WALLET_MAGNET_BONUS,3));


    //Trading Code
    public static final DeferredItem<Item> TRADING_CORE = registerBasic("trading_core");


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