package io.github.lightman314.lightmanscurrency.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.item.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSource;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

public final class LCDataComponents {

    private LCDataComponents() {}

    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,LCApi.MODID);

    //Active Data


    public static final DeferredHolder<DataComponentType<?>,DataComponentType<StoredTrader>> STORED_TRADER = register("stored_trader",builder -> builder.persistent(StoredTrader.CODEC).networkSynchronized(StoredTrader.STREAM_CODEC));

    //Item Attributes
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<UpgradeType>> UPGRADE_TYPE = registerRegistryEntry("upgrade",LCRegistries.Upgrades.UPGRADES);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<NumberSource>> CAPACITY_BONUS = register("upgrade_capacity",builder -> builder.persistent(NumberSource.CODEC).networkSynchronized(NumberSource.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Float>> CHOCOLATE_HEALING = registerFloat("chocolate_coin_healing");
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<MobEffectInstance>>> CHOCOLATE_EFFECTS = register("chocolate_coin_effects",builder -> builder
            .persistent(MobEffectInstance.CODEC.listOf())
            .networkSynchronized(MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())));

    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) { return REGISTER.registerComponentType(name,builder); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Unit>> registerUnit(String name) { return register(name, builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> registerBool(String name) { return register(name, builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> registerInt(String name) { return register(name,builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Long>> registerLong(String name) { return register(name,builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.LONG)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Float>> registerFloat(String name) { return register(name,builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Identifier>> registerID(String name) { return register(name, builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)); }
    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> registerRegistryEntry(String name,Registry<T> registry) { return register(name, builder -> builder.persistent(registry.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(registry.key()))); }

}