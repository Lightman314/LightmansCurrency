package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.api.upgrades.*;
import io.github.lightman314.lightmanscurrency.common.items.data.*;
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
    public static final Supplier<DataComponentType<UpgradeData>> UPGRADE_DATA;
    public static final Supplier<DataComponentType<ExchangeUpgradeData>> EXCHANGE_UPGRADE_DATA;
    public static final Supplier<DataComponentType<SecurityUpgradeData>> SECURITY_UPGRADE_DATA;

    static {
        WALLET_DATA = ModRegistries.DATA_COMPONENTS.register("wallet_data", () -> new DataComponentType.Builder<WalletData>().persistent(WalletData.CODEC).build());
        CASH_REGISTER_TRADER_POSITIONS = ModRegistries.DATA_COMPONENTS.register("cash_register_trader_positions", () -> new DataComponentType.Builder<List<BlockPos>>().persistent(BlockPos.CODEC.listOf()).build());
        COIN_JAR_CONTENTS = ModRegistries.DATA_COMPONENTS.register("coin_jar_contents", () -> new DataComponentType.Builder<List<ItemStack>>().persistent(ItemStack.OPTIONAL_CODEC.listOf()).cacheEncoding().build());
        TICKET_DATA = ModRegistries.DATA_COMPONENTS.register("ticket_data", () -> new DataComponentType.Builder<TicketData>().persistent(TicketData.CODEC).networkSynchronized(TicketData.STREAM_CODEC).cacheEncoding().build());
        UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("upgrade_data", () -> new DataComponentType.Builder<UpgradeData>().persistent(UpgradeData.CODEC).build());
        EXCHANGE_UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("exchange_upgrade_data", () -> new DataComponentType.Builder<ExchangeUpgradeData>().persistent(ExchangeUpgradeData.CODEC).build());
        SECURITY_UPGRADE_DATA = ModRegistries.DATA_COMPONENTS.register("security_upgrade_data", () -> new DataComponentType.Builder<SecurityUpgradeData>().persistent(SecurityUpgradeData.CODEC).build());
    }


}
