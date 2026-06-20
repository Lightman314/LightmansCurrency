package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.chocolate_coins.ChocolateCoinItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    //Trading Code
    public static final DeferredItem<Item> TRADING_CORE = registerBasic("trading_core");

    public static DeferredItem<Item> registerBasic(String id) { return register(id,Item::new); }
    public static DeferredItem<Item> registerBasic(String id,UnaryOperator<Item.Properties> propertyBuilder) { return register(id,Item::new,propertyBuilder); }

    public static <T extends Item> DeferredItem<T> register(String id, Function<Item.Properties,T> factory) { return register(id,factory,UnaryOperator.identity()); }
    public static <T extends Item> DeferredItem<T> register(String id, Function<Item.Properties,T> factory, UnaryOperator<Item.Properties> propertyBuilder) {
        return REGISTER.registerItem(id,factory,propertyBuilder);
    }

    public static void addAlias(String oldName,String newName) { REGISTER.addAlias(LCApi.id(oldName),LCApi.id(newName)); }

}