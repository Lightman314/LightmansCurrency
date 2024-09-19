package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.upgrades.*;
import io.github.lightman314.lightmanscurrency.common.items.data.*;
import io.github.lightman314.lightmanscurrency.common.items.experimental.ATMCardData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {

    public static void init() {}

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

    static {
        WALLET_DATA = ModRegistries.DATA_COMPONENTS.register("wallet_data", () -> new DataComponentType.Builder<WalletData>().persistent(WalletData.CODEC).build());
        CASH_REGISTER_TRADER_POSITIONS = ModRegistries.DATA_COMPONENTS.register("cash_register_trader_positions", () -> new DataComponentType.Builder<List<BlockPos>>().persistent(BlockPos.CODEC.listOf()).build());
        COIN_JAR_CONTENTS = ModRegistries.DATA_COMPONENTS.register("coin_jar_contents", () -> new DataComponentType.Builder<List<ItemStack>>().persistent(ItemStack.OPTIONAL_CODEC.listOf()).cacheEncoding().build());
        TICKET_DATA = ModRegistries.DATA_COMPONENTS.register("ticket_data", () -> new DataComponentType.Builder<TicketData>().persistent(TicketData.CODEC).networkSynchronized(TicketData.STREAM_CODEC).cacheEncoding().build());
        TRADER_ITEM_DATA = ModRegistries.DATA_COMPONENTS.register("trader_data", () -> new DataComponentType.Builder<TraderItemData>().persistent(TraderItemData.CODEC).networkSynchronized(TraderItemData.STREAM_CODEC).cacheEncoding().build());
        UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("upgrade_data", () -> new DataComponentType.Builder<UpgradeData>().persistent(UpgradeData.CODEC).build());
        EXCHANGE_UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("exchange_upgrade_data", () -> new DataComponentType.Builder<ExchangeUpgradeData>().persistent(ExchangeUpgradeData.CODEC).networkSynchronized(ExchangeUpgradeData.STREAM_CODEC).build());
        SECURITY_UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("security_upgrade_data", () -> new DataComponentType.Builder<SecurityUpgradeData>().persistent(SecurityUpgradeData.CODEC).networkSynchronized(SecurityUpgradeData.STREAM_CODEC).build());
        BANK_UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("bank_upgrade_data", () -> new DataComponentType.Builder<BankUpgradeData>().persistent(BankUpgradeData.CODEC).networkSynchronized(BankUpgradeData.STREAM_CODEC).build());
        UPGRADE_ACTIVE = ModRegistries.DATA_COMPONENTS.register("upgrade_active", () -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).build());
        ATM_CARD_DATA = ModRegistries.DATA_COMPONENTS.register("atm_card_data", () -> new DataComponentType.Builder<ATMCardData>().persistent(ATMCardData.CODEC).networkSynchronized(ATMCardData.STREAM_CODEC).build());
        MONEY_VALUE = ModRegistries.DATA_COMPONENTS.register("money_value", () -> new DataComponentType.Builder<MoneyValue>().persistent(LCCodecs.MONEY_VALUE).networkSynchronized(LCCodecs.MONEY_VALUE_STREAM).build());
    }

}
