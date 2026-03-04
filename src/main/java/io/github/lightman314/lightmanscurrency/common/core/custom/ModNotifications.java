package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.categories.TaxEntryCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.DeprecatedNotificationTypes;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TextNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModNotifications {

    public static final DeferredRegister<NotificationType<?>> TYPES = DeferredRegister.create(LCRegistries.NOTIFICATION_TYPES,LightmansCurrency.MODID);
    public static final DeferredRegister<NotificationCategoryType<?>> CATEGORIES = DeferredRegister.create(LCRegistries.NOTIFICATION_CATEGORIES,LightmansCurrency.MODID);

    static {
        TYPES.register("item_trade",() -> ItemTradeNotification.TYPE);
        TYPES.register("paygate_trade",() -> PaygateNotification.TYPE);
        TYPES.register("slot_machine_trade",() -> SlotMachineTradeNotification.TYPE);
        TYPES.register("out_of_stock",() -> OutOfStockNotification.TYPE);
        TYPES.register("bank_low_balance",() -> LowBalanceNotification.TYPE);
        TYPES.register("auction_house_seller",() -> AuctionHouseSellerNotification.TYPE);
        TYPES.register("auction_house_buyer",() -> AuctionHouseBuyerNotification.TYPE);
        TYPES.register("auction_house_seller_nobid",() -> AuctionHouseSellerNobidNotification.TYPE);
        TYPES.register("auction_house_outbid",() -> AuctionHouseBidNotification.TYPE);
        TYPES.register("auction_house_canceled",() -> AuctionHouseCancelNotification.TYPE);
        TYPES.register("text",() -> TextNotification.TYPE);
        TYPES.register("add_remove_ally",() -> AddRemoveAllyNotification.TYPE);
        TYPES.register("add_remove_trade",() -> DeprecatedNotificationTypes.ADD_REMOVE_TRADE);
        TYPES.register("change_ally_permissions",() -> ChangeAllyPermissionNotification.TYPE);
        TYPES.register("change_creative",() -> ChangeCreativeNotification.TYPE);
        TYPES.register("changed_name",() -> ChangeNameNotification.TYPE);
        TYPES.register("change_ownership",() -> ChangeOwnerNotification.TYPE);
        TYPES.register("change_settings_simple",() -> ChangeSettingNotification.SIMPLE_TYPE);
        TYPES.register("change_settings_advanced",() -> ChangeSettingNotification.ADVANCED_TYPE);
        TYPES.register("change_settings_dumb",() -> ChangeSettingNotification.DUMB_TYPE);
        TYPES.register("bank_deposit_player",() -> DepositWithdrawNotification.PLAYER_TYPE);
        TYPES.register("bank_deposit_trader",() -> DepositWithdrawNotification.CUSTOM_TYPE);
        TYPES.register("bank_deposit_server",() -> DepositWithdrawNotification.SERVER_TYPE);
        TYPES.register("bank_transfer",() -> BankTransferNotification.TYPE);
        TYPES.register("bank_interest",() -> BankInterestNotification.TYPE);
        TYPES.register("taxes_collected",() -> TaxesCollectedNotification.TYPE);
        TYPES.register("taxes_paid",() -> TaxesPaidNotification.TYPE);
        TYPES.register("block_ejected",() -> OwnableBlockEjectedNotification.TYPE);
        TYPES.register("command_trade",() -> CommandTradeNotification.TYPE);
        TYPES.register("gacha_trade",() -> GachaTradeNotification.TYPE);
        TYPES.register("bank_salary_payment",() -> SalaryPaymentNotification.TYPE);

        //Initialize the Notification Category deserializers
        CATEGORIES.register("general",() -> NotificationCategory.GENERAL_TYPE);
        CATEGORIES.register("null",() -> NullCategory.TYPE);
        CATEGORIES.register("seasonal_event",() -> EventCategory.TYPE);
        CATEGORIES.register("trader",() -> TraderCategory.TYPE);
        CATEGORIES.register("bank",() -> BankCategory.TYPE);
        CATEGORIES.register("auction_house",() -> AuctionHouseCategory.TYPE);
        CATEGORIES.register("tax_entry",() -> TaxEntryCategory.TYPE);
    }

}
