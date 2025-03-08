package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.upgrades.*;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.items.data.*;
import io.github.lightman314.lightmanscurrency.common.items.data.ATMCardData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static void init() {}

    //Active Data
    public static final Supplier<DataComponentType<WalletData>> WALLET_DATA;
    public static final Supplier<DataComponentType<List<BlockPos>>> CASH_REGISTER_TRADER_POSITIONS;
    public static final Supplier<DataComponentType<List<ItemStack>>> COIN_JAR_CONTENTS;
    public static final Supplier<DataComponentType<TicketData>> TICKET_DATA;
    public static final Supplier<DataComponentType<TraderItemData>> TRADER_ITEM_DATA;
    public static final Supplier<DataComponentType<UpgradeData>> UPGRADE_DATA;
    public static final Supplier<DataComponentType<ExchangeUpgradeData>> EXCHANGE_UPGRADE_DATA;
    public static final Supplier<DataComponentType<SecurityUpgradeData>> SECURITY_UPGRADE_DATA;
    public static final Supplier<DataComponentType<BankUpgradeData>> BANK_UPGRADE_DATA;
    public static final Supplier<DataComponentType<Boolean>> UPGRADE_ACTIVE;
    public static final Supplier<DataComponentType<ATMCardData>> ATM_CARD_DATA;
    public static final Supplier<DataComponentType<MoneyValue>> MONEY_VALUE;
    public static final Supplier<DataComponentType<ItemStackData>> GACHA_ITEM;

    //Item Attributes
    public static final Supplier<DataComponentType<Integer>> WALLET_LEVEL;
    public static final Supplier<DataComponentType<Integer>> WALLET_CAPACITY;
    public static final Supplier<DataComponentType<Boolean>> WALLET_INVULNERABLE;
    public static final Supplier<DataComponentType<Integer>> WALLET_BONUS_MAGNET;
    public static final Supplier<DataComponentType<Integer>> WALLET_UPGRADE_LIMIT;
    public static final Supplier<DataComponentType<ResourceLocation>> WALLET_MODEL;
    public static final Supplier<DataComponentType<List<SoundEntry>>> WALLET_COIN_SOUND;
    public static final Supplier<DataComponentType<List<MobEffectInstance>>> CHOCOLATE_EFFECTS;
    public static final Supplier<DataComponentType<Float>> CHOCOLATE_HEALING;
    public static final Supplier<DataComponentType<AncientCoinType>> ANCIENT_COIN_TYPE;

    static {

        //Live Data
        WALLET_DATA = register("wallet_data", builder -> builder.persistent(WalletData.CODEC).networkSynchronized(WalletData.STREAM_CODEC));
        CASH_REGISTER_TRADER_POSITIONS = register("cash_register_trader_positions", builder -> builder.persistent(BlockPos.CODEC.listOf()));
        COIN_JAR_CONTENTS = register("coin_jar_contents", builder -> builder.persistent(ItemStack.OPTIONAL_CODEC.listOf()).cacheEncoding());
        TICKET_DATA = register("ticket_data", builder -> builder.persistent(TicketData.CODEC).networkSynchronized(TicketData.STREAM_CODEC).cacheEncoding());
        TRADER_ITEM_DATA = register("trader_data", builder -> builder.persistent(TraderItemData.CODEC).networkSynchronized(TraderItemData.STREAM_CODEC).cacheEncoding());
        UPGRADE_DATA = register("upgrade_data", builder -> builder.persistent(UpgradeData.CODEC).networkSynchronized(UpgradeData.STREAM_CODEC));
        EXCHANGE_UPGRADE_DATA = register("exchange_upgrade_data", builder -> builder.persistent(ExchangeUpgradeData.CODEC).networkSynchronized(ExchangeUpgradeData.STREAM_CODEC));
        SECURITY_UPGRADE_DATA = register("security_upgrade_data", builder -> builder.persistent(SecurityUpgradeData.CODEC).networkSynchronized(SecurityUpgradeData.STREAM_CODEC));
        BANK_UPGRADE_DATA = register("bank_upgrade_data", builder -> builder.persistent(BankUpgradeData.CODEC).networkSynchronized(BankUpgradeData.STREAM_CODEC));
        UPGRADE_ACTIVE = registerBool("upgrade_active");
        ATM_CARD_DATA = register("atm_card_data", builder -> builder.persistent(ATMCardData.CODEC).networkSynchronized(ATMCardData.STREAM_CODEC));
        MONEY_VALUE = register("money_value", builder -> builder.persistent(LCCodecs.MONEY_VALUE).networkSynchronized(LCCodecs.MONEY_VALUE_STREAM));
        GACHA_ITEM = register("gacha_item", builder -> builder.persistent(ItemStackData.CODEC).networkSynchronized(ItemStackData.STREAM_CODEC));

        //Custom Item Attributes
        WALLET_LEVEL = registerInt("wallet_level");
        WALLET_CAPACITY = registerInt("wallet_capacity");
        WALLET_INVULNERABLE = registerBool("wallet_invulnerable");
        WALLET_BONUS_MAGNET = registerInt("wallet_bonus_magnet");
        WALLET_UPGRADE_LIMIT = registerInt("wallet_upgrade_limit");
        WALLET_MODEL = registerResource("wallet_model");
        WALLET_COIN_SOUND = register("wallet_coin_sound", builder -> builder.persistent(SoundEntry.CODEC.listOf()));
        CHOCOLATE_EFFECTS = register("chocolate_effects", builder -> builder.persistent(MobEffectInstance.CODEC.listOf()));
        CHOCOLATE_HEALING = registerFloat("chocolate_healing");
        ANCIENT_COIN_TYPE = register("ancient_coin_type", builder -> builder.persistent(AncientCoinType.CODEC));

    }

    private static <T> Supplier<DataComponentType<T>> register(@Nonnull String name, @Nonnull UnaryOperator<DataComponentType.Builder<T>> builder) { return ModRegistries.DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build()); }
    private static Supplier<DataComponentType<Boolean>> registerBool(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.BOOL).networkSynchronized(StreamCodec.of(FriendlyByteBuf::writeBoolean,FriendlyByteBuf::readBoolean))); }
    private static Supplier<DataComponentType<Integer>> registerInt(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.INT).networkSynchronized(StreamCodec.of(FriendlyByteBuf::writeInt,FriendlyByteBuf::readInt))); }
    private static Supplier<DataComponentType<Float>> registerFloat(@Nonnull String name) { return register(name,builder -> builder.persistent(Codec.FLOAT).networkSynchronized(StreamCodec.of(FriendlyByteBuf::writeFloat,FriendlyByteBuf::readFloat))); }
    private static Supplier<DataComponentType<ResourceLocation>> registerResource(@Nonnull String name) { return register(name,builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)); }

}
