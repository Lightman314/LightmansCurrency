package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.upgrades.*;
import io.github.lightman314.lightmanscurrency.common.items.data.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.items.data.*;
import io.github.lightman314.lightmanscurrency.common.items.data.ATMCardData;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionList;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, LightmansCurrency.MODID);

    //Active Data
    public static final Supplier<DataComponentType<WalletData>> WALLET_DATA = register("wallet_data", builder -> builder
            .persistent(WalletData.CODEC)
            .networkSynchronized(WalletData.STREAM_CODEC));
    public static final Supplier<DataComponentType<List<BlockPos>>> CASH_REGISTER_TRADER_POSITIONS = register("cash_register_trader_positions", builder -> builder
            .persistent(BlockPos.CODEC.listOf())
            .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .cacheEncoding());
    public static final Supplier<DataComponentType<List<ItemStack>>> COIN_JAR_CONTENTS = register("coin_jar_contents",builder -> builder
            .persistent(ItemStack.OPTIONAL_CODEC.listOf())
            .networkSynchronized(ItemStack.OPTIONAL_LIST_STREAM_CODEC)
            .cacheEncoding());
    /**
     * @deprecated Use {@link ModDataComponents#TICKET_ID} and vanilla {@link net.minecraft.core.component.DataComponents#DYED_COLOR DataComponents#DYED_COLOR} instead
     */
    @Deprecated(since = "2.2.6.3")
    public static final Supplier<DataComponentType<TicketData>> TICKET_DATA = register("ticket_data", builder -> builder
            .persistent(TicketData.CODEC)
            .cacheEncoding());
    public static final Supplier<DataComponentType<Long>> TICKET_ID = registerLong("ticket_id");
    public static final Supplier<DataComponentType<Integer>> TICKET_USES = registerInt("ticket_uses");
    /**
     * @deprecated Use {@link ModDataComponents#COUPON_CODE} and vanilla {@link net.minecraft.core.component.DataComponents#DYED_COLOR DataComponents#DYED_COLOR} instead
     */
    @Deprecated(since = "2.2.6.3")
    public static final Supplier<DataComponentType<CouponData>> COUPON_DATA = register("coupon_data", builder -> builder
            .persistent(CouponData.CODEC)
            .cacheEncoding());
    public static final Supplier<DataComponentType<Integer>> COUPON_CODE = registerInt("coupon_id");
    public static final Supplier<DataComponentType<TraderItemData>> TRADER_ITEM_DATA = register("trader_data", builder -> builder
            .persistent(TraderItemData.CODEC)
            .networkSynchronized(TraderItemData.STREAM_CODEC)
            .cacheEncoding());
    public static final Supplier<DataComponentType<UpgradeData>> UPGRADE_DATA = register("upgrade_data", builder -> builder
            .persistent(UpgradeData.CODEC)
            .networkSynchronized(UpgradeData.STREAM_CODEC)
            .cacheEncoding());
    public static final Supplier<DataComponentType<ExchangeUpgradeData>> EXCHANGE_UPGRADE_DATA = register("exchange_upgrade_data", builder -> builder
            .persistent(ExchangeUpgradeData.CODEC)
            .networkSynchronized(ExchangeUpgradeData.STREAM_CODEC));
    public static final Supplier<DataComponentType<SecurityUpgradeData>> SECURITY_UPGRADE_DATA = register("security_upgrade_data", builder -> builder
            .persistent(SecurityUpgradeData.CODEC)
            .networkSynchronized(SecurityUpgradeData.STREAM_CODEC));
    public static final Supplier<DataComponentType<BankUpgradeData>> BANK_UPGRADE_DATA = register("bank_upgrade_data", builder -> builder
            .persistent(BankUpgradeData.CODEC)
            .networkSynchronized(BankUpgradeData.STREAM_CODEC));
    public static final Supplier<DataComponentType<Boolean>> UPGRADE_ACTIVE = registerBool("upgrade_active");
    public static final Supplier<DataComponentType<ATMCardData>> ATM_CARD_DATA = register("atm_card_data", builder -> builder
            .persistent(ATMCardData.CODEC)
            .networkSynchronized(ATMCardData.STREAM_CODEC));
    public static final Supplier<DataComponentType<MoneyValue>> MONEY_VALUE = register("money_value", builder -> builder
            .persistent(MoneyValue.CODEC)
            .networkSynchronized(MoneyValue.STREAM_CODEC));
    public static final Supplier<DataComponentType<ItemStackData>> GACHA_ITEM = register("gacha_item", builder -> builder
            .persistent(ItemStackData.CODEC)
            .networkSynchronized(ItemStackData.STREAM_CODEC));
    public static final Supplier<DataComponentType<MoneyBagData>> MONEY_BAG_CONTENTS = register("money_bag_data", builder -> builder
            .persistent(MoneyBagData.CODEC)
            .networkSynchronized(MoneyBagData.STREAM_CODEC));
    public static final Supplier<DataComponentType<LootTableEntry>> LOOT_TABLE_ENTRY = register("loot_table", builder -> builder
            .persistent(LootTableEntry.CODEC)
            .networkSynchronized(LootTableEntry.STREAM_CODEC));
    public static final Supplier<DataComponentType<ResourceLocation>> MODEL_VARIANT = registerResource("model_variant");
    public static final Supplier<DataComponentType<Unit>> VARIANT_LOCK = registerUnit("variant_lock");
    public static final Supplier<DataComponentType<SavedSettingData>> SETTINGS_DATA = register("settings_data",builder -> builder
            .persistent(SavedSettingData.CODEC)
            .networkSynchronized(SavedSettingData.STREAM_CODEC));
    public static final Supplier<DataComponentType<FilterData>> FILTER_DATA = register("filter_data",builder -> builder
            .persistent(FilterData.CODEC)
            .networkSynchronized(FilterData.STREAM_CODEC));
    public static final Supplier<DataComponentType<TransactionList>> REGISTER_TRANSACTIONS = register("register_transactions",builder -> builder
            .persistent(TransactionList.CODEC)
            .networkSynchronized(TransactionList.STREAM_CODEC));

    //Item Attributes
    public static final Supplier<DataComponentType<Integer>> WALLET_CAPACITY = registerInt("wallet_capacity");
    public static final Supplier<DataComponentType<Boolean>> WALLET_INVULNERABLE = registerBool("wallet_invulnerable");
    public static final Supplier<DataComponentType<Integer>> WALLET_BONUS_MAGNET = registerInt("wallet_bonus_magnet");
    public static final Supplier<DataComponentType<Integer>> WALLET_UPGRADE_LIMIT = registerInt("wallet_upgrade_limit");
    public static final Supplier<DataComponentType<ResourceLocation>> WALLET_MODEL = registerResource("wallet_model");
    public static final Supplier<DataComponentType<List<SoundEntry>>> WALLET_COIN_SOUND = register("wallet_coin_sound", builder -> builder
            .persistent(SoundEntry.CODEC.listOf())
            .networkSynchronized(SoundEntry.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final Supplier<DataComponentType<List<MobEffectInstance>>> CHOCOLATE_EFFECTS = register("chocolate_effects", builder -> builder
            .persistent(MobEffectInstance.CODEC.listOf())
            .networkSynchronized(MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final Supplier<DataComponentType<Float>> CHOCOLATE_HEALING = registerFloat("chocolate_healing");
    public static final Supplier<DataComponentType<AncientCoinType>> ANCIENT_COIN_TYPE = register("ancient_coin_type", builder -> builder
            .persistent(AncientCoinType.CODEC)
            .networkSynchronized(AncientCoinType.STREAM_CODEC));
    public static final Supplier<DataComponentType<Unit>> ANCIENT_COIN_RANDOM = registerUnit("ancient_coin_random");

    private static <T> Supplier<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) { return REGISTER.register(name, () -> builder.apply(DataComponentType.builder()).build()); }
    private static Supplier<DataComponentType<Unit>> registerUnit(String name) { return register(name,builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))); }
    private static Supplier<DataComponentType<Boolean>> registerBool(String name) { return register(name,builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)); }
    private static Supplier<DataComponentType<Integer>> registerInt(String name) { return register(name,builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)); }
    private static Supplier<DataComponentType<Long>> registerLong(String name) { return register(name,builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)); }
    private static Supplier<DataComponentType<Float>> registerFloat(String name) { return register(name,builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)); }
    private static Supplier<DataComponentType<ResourceLocation>> registerResource(String name) { return register(name,builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)); }
}
