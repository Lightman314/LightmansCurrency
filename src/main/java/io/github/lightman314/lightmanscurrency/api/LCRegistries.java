package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class LCRegistries {

    public static final ResourceKey<Registry<EjectionDataType<?>>> EJECTION_DATA_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("ejection_data"));
    public static final Registry<EjectionDataType<?>> EJECTION_DATA = new RegistryBuilder<>(EJECTION_DATA_KEY).create();

    public static final ResourceKey<Registry<CustomDataType<?>>> CUSTOM_DATA_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("custom_data"));
    public static final Registry<CustomDataType<?>> CUSTOM_DATA = new RegistryBuilder<>(CUSTOM_DATA_KEY).create();

    //Notifications have Stream Codecs, so sync the ids
    public static final ResourceKey<Registry<NotificationType<?>>> NOTIFICATION_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("notification_type"));
    public static final Registry<NotificationType<?>> NOTIFICATION_TYPES = new RegistryBuilder<>(NOTIFICATION_TYPE_KEY).sync(true).create();

    //Notifications have Stream Codecs, so sync the ids
    public static final ResourceKey<Registry<NotificationCategoryType<?>>> NOTIFICATION_CATEGORY_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("notification_category"));
    public static final Registry<NotificationCategoryType<?>> NOTIFICATION_CATEGORIES = new RegistryBuilder<>(NOTIFICATION_CATEGORY_KEY).sync(true).create();

    public static final ResourceKey<Registry<BankReferenceType<?>>> BANK_REFERENCE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("bank_reference"));
    public static final Registry<BankReferenceType<?>> BANK_REFERENCE = new RegistryBuilder<>(BANK_REFERENCE_KEY).sync(true).create();

    //Owners may be synced
    public static final ResourceKey<Registry<OwnerType<?>>> OWNER_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("owner_type"));
    public static final Registry<OwnerType<?>> OWNER_TYPES = new RegistryBuilder<>(OWNER_TYPE_KEY).sync(true).create();

    public static final ResourceKey<Registry<TraderType<?>>> TRADER_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("trader_type"));
    public static final Registry<TraderType<?>> TRADER_TYPES = new RegistryBuilder<>(TRADER_TYPE_KEY).create();

    public static final ResourceKey<Registry<TraderNodeType<?>>> TRADER_NODE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("trader_nodes"));
    public static final Registry<TraderNodeType<?>> TRADER_NODE = new RegistryBuilder<>(TRADER_NODE_KEY).create();

    public static final ResourceKey<Registry<TradeRuleType<?>>> TRADE_RULE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("trade_rule"));
    public static final Registry<TradeRuleType<?>> TRADE_RULE = new RegistryBuilder<>(TRADE_RULE_KEY).sync(true).create();

    public static final ResourceKey<Registry<ItemTradeType<?>>> ITEM_TRADE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("item_trade"));
    public static final Registry<ItemTradeType<?>> ITEM_TRADE = new RegistryBuilder<>(ITEM_TRADE_KEY).sync(true).create();

    public static final ResourceKey<Registry<StatType<?,?>>> STAT_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("stat_type"));
    public static final Registry<StatType<?,?>> STAT_TYPES = new RegistryBuilder<>(STAT_TYPE_KEY).sync(true).create();

    public static final ResourceKey<Registry<LazyPacketType<?>>> LAZY_PACKETS_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("lazy_packets"));
    public static final Registry<LazyPacketType<?>> LAZY_PACKETS = new RegistryBuilder<>(LAZY_PACKETS_KEY).sync(true).create();

    public static final ResourceKey<Registry<IconType<?>>> ICON_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("icon_type"));
    public static final Registry<IconType<?>> ICON_TYPE = new RegistryBuilder<>(ICON_TYPE_KEY).sync(true).create();

    public static final ResourceKey<Registry<ATMIconType>> ATM_ICON_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("atm_icon_type"));
    public static final Registry<ATMIconType> ATM_ICON_TYPE = new RegistryBuilder<>(ATM_ICON_TYPE_KEY).create();

    public static final ResourceKey<Registry<CurrencyType<?>>> CURRENCY_TYPE_KEY = ResourceKey.createRegistryKey(LightmansCurrency.id("money_type"));
    public static final Registry<CurrencyType<?>> CURRENCY_TYPE = new RegistryBuilder<>(CURRENCY_TYPE_KEY).sync(true).create();

}