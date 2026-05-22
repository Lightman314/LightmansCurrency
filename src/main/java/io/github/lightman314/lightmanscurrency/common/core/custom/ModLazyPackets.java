package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.salary.SalaryData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.stats.StatTracker;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.menus.slot_machine.ResultHolder;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.trade.GachaDummyTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineDummyTrade;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ModLazyPackets {

    public static final DeferredRegister<LazyPacketType<?>> REGISTER = DeferredRegister.create(LCRegistries.LAZY_PACKETS, LightmansCurrency.MODID);

    public static final Supplier<LazyPacketType<Component>> TEXT = register("text",() -> ComponentSerialization.STREAM_CODEC);
    public static final Supplier<LazyPacketType<ItemStack>> ITEM_STACK = register("itemstack",() -> ItemStack.OPTIONAL_STREAM_CODEC);
    public static final Supplier<LazyPacketType<Optional<Item>>> ITEM_OPTIONAL = register("optional_item",() -> StreamHelper.OPTIONAL_ITEM_STREAM);
    public static final Supplier<LazyPacketType<BlockPos>> BLOCK_POS = register("blockpos",() -> BlockPos.STREAM_CODEC);
    public static final Supplier<LazyPacketType<WorldPosition>> WORLD_POS = register("worldpos",() -> WorldPosition.STREAM_CODEC);

    public static final Supplier<LazyPacketType<MoneyValue>> MONEY_VALUE = register("money",() -> MoneyValue.STREAM_CODEC);

    public static final Supplier<LazyPacketType<Owner>> OWNER = register("owner",() -> Owner.STREAM_CODEC);
    public static final Supplier<LazyPacketType<OwnerData>> OWNER_DATA = register("owner_data",() -> OwnerData.STREAM_CODEC);

    public static final Supplier<LazyPacketType<PlayerReference>> PLAYER_REFERENCE = register("player_reference",() -> PlayerReference.STREAM_CODEC);
    public static final Supplier<LazyPacketType<BankReference>> BANK_REFERENCE = register("bank_reference",() -> BankReference.STREAM_CODEC);

    public static final Supplier<LazyPacketType<Notification>> NOTIFICATION = register("notification",() -> Notification.STREAM_CODEC);
    public static final Supplier<LazyPacketType<NotificationCategory>> NOTIFICATION_CATEGORY = register("notification_category",() -> NotificationCategory.STREAM_CODEC);
    public static final Supplier<LazyPacketType<NotificationData>> NOTIFICATION_DATA = register("notification_data",() -> NotificationData.STREAM_CODEC);

    public static final Supplier<LazyPacketType<IconData>> ICON = register("icon",() -> IconData.STREAM_CODEC);
    public static final Supplier<LazyPacketType<Map<String,StatType.Instance<?,?>>>> STAT_TRACKER = register("stats",() -> StatTracker.STREAM_CODEC);
    public static final Supplier<LazyPacketType<EjectionData>> EJECTION_DATA = register("ejection_data",() -> EjectionData.STREAM_CODEC);
    public static final Supplier<LazyPacketType<DirectionalSettingsState>> DIRECTIONAL_SETTINGS = register("directional_settings",() -> DirectionalSettingsState.STREAM_CODEC);
    public static final Supplier<LazyPacketType<NodeSelections>> SETTINGS_NODE_SELECTIONS = register("settings_node_selections",() -> NodeSelections.STREAM_CODEC);

    public static final Supplier<LazyPacketType<AuctionTradeData>> AUCTION_TRADE = register("auction_trade",() -> AuctionTradeData.STREAM_CODEC);
    public static final Supplier<LazyPacketType<AuctionPlayerStorage>> AUCTION_STORAGE = register("auction_storage",() -> AuctionPlayerStorage.STREAM_CODEC);

    public static final Supplier<LazyPacketType<ItemTradeData>> ITEM_TRADE = register("item_trade",() -> ItemTradeData.STREAM_CODEC);

    public static final Supplier<LazyPacketType<PaygateTradeData>> PAYGATE_TRADE = register("paygate_trade",() -> PaygateTradeData.STREAM_CODEC);

    public static final Supplier<LazyPacketType<CommandTrade>> COMMAND_TRADE = register("command_trade",() -> CommandTrade.STREAM_CODEC);

    public static final Supplier<LazyPacketType<SlotMachineDummyTrade>> SLOT_MACHINE_DUMMY = register("slot_machine_dummy",() -> SlotMachineDummyTrade.STREAM_CODEC);
    public static final Supplier<LazyPacketType<SlotMachineEntry>> SLOT_MACHINE_ENTRY = register("slot_machine_entry",() -> SlotMachineEntry.STREAM_CODEC);
    public static final Supplier<LazyPacketType<ResultHolder>> SLOT_MACHINE_RESULT = register("slot_machine_result",() -> ResultHolder.STREAM_CODEC);
    public static final Supplier<LazyPacketType<GachaDummyTrade>> GACHA_MACHINE_DUMMY = register("gacha_machine_dummy",() -> GachaDummyTrade.STREAM_CODEC);

    public static final Supplier<LazyPacketType<BankAccount>> BANK_ACCOUNT = register("bank_account",() -> BankAccount.STREAM_CODEC);
    public static final Supplier<LazyPacketType<SalaryData>> SALARY_DATA = register("salary_data",() -> SalaryData.STREAM_CODEC);

    public static final Supplier<LazyPacketType<Team>> TEAM = register("team",() -> Team.STREAM_CODEC);

    public static final Supplier<LazyPacketType<TaxableReference>> TAXABLE_REFERENCE = register("taxable_reference",() -> TaxableReference.STREAM_CODEC);

    public static <T> Supplier<LazyPacketType<T>> register(String id, StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { return register(id,() -> codec); }
    public static <T> Supplier<LazyPacketType<T>> register(String id, Supplier<StreamCodec<? super RegistryFriendlyByteBuf,T>> codec) {
        return REGISTER.register(id,() -> new LazyPacketType<>(codec.get()));
    }

}
