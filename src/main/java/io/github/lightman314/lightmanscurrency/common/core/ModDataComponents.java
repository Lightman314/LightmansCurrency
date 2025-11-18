package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.upgrades.*;
import io.github.lightman314.lightmanscurrency.common.data.types.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.items.data.*;
import io.github.lightman314.lightmanscurrency.common.items.data.ATMCardData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("deprecation")
public class ModDataComponents {

    public static void init() {}

    //Active Data
    public static final Supplier<DataComponentType<WalletData>> WALLET_DATA;
    public static final Supplier<DataComponentType<List<BlockPos>>> CASH_REGISTER_TRADER_POSITIONS;
    public static final Supplier<DataComponentType<List<ItemStack>>> COIN_JAR_CONTENTS;
    /**
     * @deprecated Use {@link ModDataComponents#TICKET_ID} and vanilla {@link net.minecraft.core.component.DataComponents#DYED_COLOR DataComponents#DYED_COLOR} instead
     */
    @Deprecated(since = "2.2.6.3")
    public static final Supplier<DataComponentType<TicketData>> TICKET_DATA;
    public static final Supplier<DataComponentType<Long>> TICKET_ID;
    public static final Supplier<DataComponentType<Integer>> TICKET_USES;
    /**
     * @deprecated Use {@link ModDataComponents#COUPON_CODE} and vanilla {@link net.minecraft.core.component.DataComponents#DYED_COLOR DataComponents#DYED_COLOR} instead
     */
    @Deprecated(since = "2.2.6.3")
    public static final Supplier<DataComponentType<CouponData>> COUPON_DATA;
    public static final Supplier<DataComponentType<Integer>> COUPON_CODE;
    public static final Supplier<DataComponentType<TraderItemData>> TRADER_ITEM_DATA;
    public static final Supplier<DataComponentType<UpgradeData>> UPGRADE_DATA;
    public static final Supplier<DataComponentType<ExchangeUpgradeData>> EXCHANGE_UPGRADE_DATA;
    public static final Supplier<DataComponentType<SecurityUpgradeData>> SECURITY_UPGRADE_DATA;
    public static final Supplier<DataComponentType<BankUpgradeData>> BANK_UPGRADE_DATA;
    public static final Supplier<DataComponentType<Boolean>> UPGRADE_ACTIVE;
    public static final Supplier<DataComponentType<ATMCardData>> ATM_CARD_DATA;
    public static final Supplier<DataComponentType<MoneyValue>> MONEY_VALUE;
    public static final Supplier<DataComponentType<ItemStackData>> GACHA_ITEM;
    public static final Supplier<DataComponentType<MoneyBagData>> MONEY_BAG_CONTENTS;
    public static final Supplier<DataComponentType<LootTableEntry>> LOOT_TABLE_ENTRY;
    public static final Supplier<DataComponentType<ResourceLocation>> MODEL_VARIANT;
    public static final Supplier<DataComponentType<Unit>> VARIANT_LOCK;
    public static final Supplier<DataComponentType<SavedSettingData>> SETTINGS_DATA;
    public static final Supplier<DataComponentType<FilterData>> FILTER_DATA;

    //Item Attributes
    public static final Supplier<DataComponentType<Integer>> WALLET_CAPACITY;
    public static final Supplier<DataComponentType<Boolean>> WALLET_INVULNERABLE;
    public static final Supplier<DataComponentType<Integer>> WALLET_BONUS_MAGNET;
    public static final Supplier<DataComponentType<Integer>> WALLET_UPGRADE_LIMIT;
    public static final Supplier<DataComponentType<ResourceLocation>> WALLET_MODEL;
    public static final Supplier<DataComponentType<List<SoundEntry>>> WALLET_COIN_SOUND;
    public static final Supplier<DataComponentType<List<MobEffectInstance>>> CHOCOLATE_EFFECTS;
    public static final Supplier<DataComponentType<Float>> CHOCOLATE_HEALING;
    public static final Supplier<DataComponentType<AncientCoinType>> ANCIENT_COIN_TYPE;
    public static final Supplier<DataComponentType<Unit>> ANCIENT_COIN_RANDOM;

    static {

        //Live Data
        WALLET_DATA = register("wallet_data", builder -> builder.persistent(WalletData.CODEC).networkSynchronized(WalletData.STREAM_CODEC));
        CASH_REGISTER_TRADER_POSITIONS = register("cash_register_trader_positions", builder -> builder.persistent(BlockPos.CODEC.listOf()));
        COIN_JAR_CONTENTS = register("coin_jar_contents", builder -> builder.persistent(ItemStack.OPTIONAL_CODEC.listOf()).cacheEncoding());
        TICKET_DATA = register("ticket_data", builder -> builder.persistent(TicketData.CODEC).networkSynchronized(TicketData.STREAM_CODEC).cacheEncoding());
        TICKET_ID = registerLong("ticket_id");
        TICKET_USES = registerInt("ticket_uses");
        COUPON_DATA = register("coupon_data", builder -> builder.persistent(CouponData.CODEC).networkSynchronized(CouponData.STREAM_CODEC).cacheEncoding());
        COUPON_CODE = registerInt("coupon_id");
        TRADER_ITEM_DATA = register("trader_data", builder -> builder.persistent(TraderItemData.CODEC).networkSynchronized(TraderItemData.STREAM_CODEC).cacheEncoding());
        UPGRADE_DATA = register("upgrade_data", builder -> builder.persistent(UpgradeData.CODEC).networkSynchronized(UpgradeData.STREAM_CODEC));
        EXCHANGE_UPGRADE_DATA = register("exchange_upgrade_data", builder -> builder.persistent(ExchangeUpgradeData.CODEC).networkSynchronized(ExchangeUpgradeData.STREAM_CODEC));
        SECURITY_UPGRADE_DATA = register("security_upgrade_data", builder -> builder.persistent(SecurityUpgradeData.CODEC).networkSynchronized(SecurityUpgradeData.STREAM_CODEC));
        BANK_UPGRADE_DATA = register("bank_upgrade_data", builder -> builder.persistent(BankUpgradeData.CODEC).networkSynchronized(BankUpgradeData.STREAM_CODEC));
        UPGRADE_ACTIVE = registerBool("upgrade_active");
        ATM_CARD_DATA = register("atm_card_data", builder -> builder.persistent(ATMCardData.CODEC).networkSynchronized(ATMCardData.STREAM_CODEC));
        MONEY_VALUE = register("money_value", builder -> builder.persistent(LCCodecs.MONEY_VALUE).networkSynchronized(LCCodecs.MONEY_VALUE_STREAM));
        GACHA_ITEM = register("gacha_item", builder -> builder.persistent(ItemStackData.CODEC).networkSynchronized(ItemStackData.STREAM_CODEC));
        MONEY_BAG_CONTENTS = register("money_bag_data", builder -> builder.persistent(MoneyBagData.CODEC).networkSynchronized(MoneyBagData.STREAM_CODEC));
        LOOT_TABLE_ENTRY = register("loot_table", builder -> builder.persistent(LootTableEntry.CODEC).networkSynchronized(LootTableEntry.STREAM_CODEC));
        MODEL_VARIANT = registerResource("model_variant");
        VARIANT_LOCK = registerUnit("variant_lock");
        SETTINGS_DATA = register("settings_data",builder -> builder.persistent(SavedSettingData.CODEC).networkSynchronized(SavedSettingData.STREAM_CODEC));
        FILTER_DATA = register("filter_data",builder -> builder.persistent(FilterData.CODEC).networkSynchronized(FilterData.STREAM_CODEC));

        //Custom Item Attributes
        WALLET_CAPACITY = registerInt("wallet_capacity");
        WALLET_INVULNERABLE = registerBool("wallet_invulnerable");
        WALLET_BONUS_MAGNET = registerInt("wallet_bonus_magnet");
        WALLET_UPGRADE_LIMIT = registerInt("wallet_upgrade_limit");
        WALLET_MODEL = registerResource("wallet_model");
        WALLET_COIN_SOUND = register("wallet_coin_sound", builder -> builder.persistent(SoundEntry.CODEC.listOf()));
        CHOCOLATE_EFFECTS = register("chocolate_effects", builder -> builder.persistent(MobEffectInstance.CODEC.listOf()));
        CHOCOLATE_HEALING = registerFloat("chocolate_healing");
        ANCIENT_COIN_TYPE = register("ancient_coin_type", builder -> builder.persistent(AncientCoinType.CODEC));
        ANCIENT_COIN_RANDOM = register("ancient_coin_random", builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    }

    private static <T> Supplier<DataComponentType<T>> register(@Nonnull String name, @Nonnull UnaryOperator<DataComponentType.Builder<T>> builder) { return ModRegistries.DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build()); }
    private static Supplier<DataComponentType<Unit>> registerUnit(@Nonnull String name) { return register(name,builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))); }
    private static Supplier<DataComponentType<Boolean>> registerBool(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)); }
    private static Supplier<DataComponentType<Integer>> registerInt(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)); }
    private static Supplier<DataComponentType<Long>> registerLong(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)); }
    private static Supplier<DataComponentType<Float>> registerFloat(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)); }
    private static Supplier<DataComponentType<ResourceLocation>> registerResource(@Nonnull String name) { return register(name,builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)); }

}
