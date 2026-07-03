package io.github.lightman314.lightmanscurrency.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.item.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSource;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorageData;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletUpgradeData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class LCDataComponents {

    private LCDataComponents() {}

    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,LCApi.MODID);

    //Active Data
    //Wallet
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> WALLET_UPGRADE_COUNT = registerIntRange("wallet_upgrades",0,WalletItem.MAX_WALLET_SLOTS - 1);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<WalletStorageData>> WALLET_CONTENTS = register("wallet_contents",WalletStorageData.CODEC,WalletStorageData.STREAM_CODEC);


    public static final DeferredHolder<DataComponentType<?>,DataComponentType<StoredTrader>> STORED_TRADER = register("stored_trader",StoredTrader.CODEC,StoredTrader.STREAM_CODEC);

    //Item Attributes
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> WALLET_CAPACITY = registerIntRange("wallet_capacity",1, WalletItem.MAX_WALLET_SLOTS);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<WalletUpgradeData>> WALLET_UPGRADE_DATA = register("wallet_upgrade_data",() -> WalletUpgradeData.CODEC,() -> WalletUpgradeData.STREAM_CODEC);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Identifier>> WALLET_MODEL = registerID("wallet_model");
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Unit>> WALLET_INVULNERABLE = registerUnit("wallet_invulnerable");
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> WALLET_MAGNET_BONUS = registerIntRange("wallet_magnet_bonus",1,255);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<UpgradeType>> UPGRADE_TYPE = registerRegistryEntry("upgrade",LCRegistries.Upgrades.UPGRADES);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<NumberSource>> CAPACITY_BONUS = register("upgrade_capacity",builder -> builder.persistent(NumberSource.CODEC).networkSynchronized(NumberSource.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Float>> CHOCOLATE_HEALING = registerFloat("chocolate_coin_healing");
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<MobEffectInstance>>> CHOCOLATE_EFFECTS = register("chocolate_coin_effects",builder -> builder
            .persistent(MobEffectInstance.CODEC.listOf())
            .networkSynchronized(MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())));

    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) { return REGISTER.registerComponentType(name,builder); }
    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) { return register(name,builder -> builder.persistent(codec).networkSynchronized(streamCodec)); }
    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> register(String name, Supplier<Codec<T>> codec, Supplier<StreamCodec<? super RegistryFriendlyByteBuf,T>> streamCodec) { return register(name, builder -> builder.persistent(codec.get()).networkSynchronized(streamCodec.get())); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Unit>> registerUnit(String name) { return register(name, builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> registerBool(String name) { return register(name, builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> registerInt(String name) { return register(name,builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> registerIntRange(String name,int min,int max) { return register(name,builder -> builder.persistent(Codec.intRange(min,max)).networkSynchronized(ByteBufCodecs.VAR_INT)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Long>> registerLong(String name) { return register(name,builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.LONG)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Float>> registerFloat(String name) { return register(name,builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)); }
    private static DeferredHolder<DataComponentType<?>,DataComponentType<Identifier>> registerID(String name) { return register(name, builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)); }
    private static <T> DeferredHolder<DataComponentType<?>,DataComponentType<T>> registerRegistryEntry(String name,Registry<T> registry) { return register(name, builder -> builder.persistent(registry.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(registry.key()))); }

}