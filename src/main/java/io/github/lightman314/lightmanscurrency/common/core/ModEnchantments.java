package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.RepairWithMoneyData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModEnchantments {

    public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, LightmansCurrency.MODID);

	public static final ResourceKey<Enchantment> MONEY_MENDING = makeKey("money_mending");
	public static final ResourceKey<Enchantment> MONEY_MENDING_CHOCOLATE = makeKey("money_mending_chocolate");
	public static final ResourceKey<Enchantment> COIN_MAGNET = makeKey("coin_magnet");

	private static ResourceKey<Enchantment> makeKey(String id) { return ResourceKey.create(Registries.ENCHANTMENT, LightmansCurrency.id(id)); }

	public static final Supplier<DataComponentType<RepairWithMoneyData>> REPAIR_WITH_MONEY = register("repair_with_money",builder -> builder.persistent(RepairWithMoneyData.CODEC));
	public static final Supplier<DataComponentType<Unit>> COLLECT_COINS = register("collect_coins_at_range",builder -> builder.persistent(Unit.CODEC));

    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) { return REGISTER.register(name,() -> builder.apply(DataComponentType.builder()).build()); }
	
}
