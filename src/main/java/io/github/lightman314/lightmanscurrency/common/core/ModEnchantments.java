package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.enchantments.data.RepairWithMoneyData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ModEnchantments {

	public static void init() {}

	public static final ResourceKey<Enchantment> MONEY_MENDING = makeKey("money_mending");
	public static final ResourceKey<Enchantment> MONEY_MENDING_CHOCOLATE = makeKey("money_mending_chocolate");
	public static final ResourceKey<Enchantment> COIN_MAGNET = makeKey("coin_magnet");

	private static ResourceKey<Enchantment> makeKey(@Nonnull String id) { return ResourceKey.create(Registries.ENCHANTMENT, VersionUtil.lcResource(id)); }

	public static final Supplier<DataComponentType<RepairWithMoneyData>> REPAIR_WITH_MONEY;
	public static final Supplier<DataComponentType<Unit>> COLLECT_COINS;

	private static HolderLookup<Item> getItemLookup() { return BuiltInRegistries.ITEM.asLookup(); }

	static {

		REPAIR_WITH_MONEY = ModRegistries.ENCHANTMENT_EFFECT_COMPONENTS.register("repair_with_money", () -> new DataComponentType.Builder<RepairWithMoneyData>().persistent(RepairWithMoneyData.CODEC).build());
		COLLECT_COINS = ModRegistries.ENCHANTMENT_EFFECT_COMPONENTS.register("collect_coins_at_range", () -> new DataComponentType.Builder<Unit>().persistent(Unit.CODEC).build());

	}
	
}
