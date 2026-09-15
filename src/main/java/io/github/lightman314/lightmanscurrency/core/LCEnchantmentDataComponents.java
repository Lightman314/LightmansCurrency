package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.enchantments.data.RepairWithMoneyData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public final class LCEnchantmentDataComponents {
    private LCEnchantmentDataComponents() {}

    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,LCApi.MODID);

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<RepairWithMoneyData>> REPAIR_WITH_MONEY = register("repair_with_money",builder -> builder.persistent(RepairWithMoneyData.CODEC).networkSynchronized(RepairWithMoneyData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Unit>> COLLECT_COINS = register("collect_coins_at_range",builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));

    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return REGISTER.registerComponentType(name,builder);
    }

}
