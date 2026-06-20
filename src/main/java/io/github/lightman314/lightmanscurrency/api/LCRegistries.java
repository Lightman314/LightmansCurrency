package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommandType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplaySerializer;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketType;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerType;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSourceType;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class LCRegistries {

    private LCRegistries() {}

    public static final class Money {
        private Money() {}

        public static final ResourceKey<Registry<MoneyValueType<?>>> VALUE_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("money_value_type"));
        public static final Registry<MoneyValueType<?>> VALUE_TYPE = new RegistryBuilder<>(VALUE_TYPE_KEY).sync(true).create();

        public static final ResourceKey<Registry<MoneyValueHelper>> VALUE_HELPER_KEY = ResourceKey.createRegistryKey(LCApi.id("money_value_helper"));
        public static final Registry<MoneyValueHelper> VALUE_HELPER = new RegistryBuilder<>(VALUE_HELPER_KEY).defaultKey(LCApi.id("default")).create();

    }

    public static final class Coins {

        private Coins() {}

        public static final ResourceKey<Registry<ValueDisplaySerializer>> VALUE_DISPLAY_SERIALIZER_KEY = ResourceKey.createRegistryKey(LCApi.id("coin_value_display_serializer"));
        public static final Registry<ValueDisplaySerializer> VALUE_DISPLAY_SERIALIZER = new RegistryBuilder<>(VALUE_DISPLAY_SERIALIZER_KEY).defaultKey(LCApi.id("null")).create();

        public static final ResourceKey<Registry<ATMIconType>> ATM_ICON_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("atm_icon_type"));
        public static final Registry<ATMIconType> ATM_ICON_TYPE = new RegistryBuilder<>(ATM_ICON_TYPE_KEY).create();

        public static final ResourceKey<Registry<ATMCommandType<?>>> ATM_COMMAND_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("atm_command_type"));
        public static final Registry<ATMCommandType<?>> ATM_COMMAND_TYPE = new RegistryBuilder<>(ATM_COMMAND_TYPE_KEY).sync(true).create();

    }

    public static final class Bank {
        private Bank() {}

        public static final ResourceKey<Registry<BankReferenceType<?>>> REFERENCE_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("bank_reference"));
        public static final Registry<BankReferenceType<?>> REFERENCE_TYPE = new RegistryBuilder<>(REFERENCE_TYPE_KEY).sync(true).create();

    }

    public static final class Ownership {
        private Ownership() {}

        public static final ResourceKey<Registry<OwnerType<?>>> OWNER_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("owner_type"));
        public static final Registry<OwnerType<?>> OWNER_TYPE = new RegistryBuilder<>(OWNER_TYPE_KEY).sync(true).create();

        public static final ResourceKey<Registry<PotentialOwnerProvider>> POTENTIAL_OWNER_KEY = ResourceKey.createRegistryKey(LCApi.id("potential_owners"));
        public static final Registry<PotentialOwnerProvider> POTENTIAL_OWNER = new RegistryBuilder<>(POTENTIAL_OWNER_KEY).create();
    }

    public static final class Trader {
        private Trader() {}

        public static final ResourceKey<Registry<TraderType>> TRADER_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("trader"));
        public static final Registry<TraderType> TRADER_TYPES = new RegistryBuilder<>(TRADER_TYPE_KEY).sync(true).defaultKey(LCApi.id("")).create();

        public static final ResourceKey<Registry<TraderNodeType<?>>> TRADER_NODE_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("trader_node"));
        public static final Registry<TraderNodeType<?>> TRADER_NODE_TYPE = new RegistryBuilder<>(TRADER_NODE_TYPE_KEY).sync(true).create();

        public static final ResourceKey<Registry<TradeDataType<?>>> TRADE_DATA_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("trade_data"));
        public static final Registry<TradeDataType<?>> TRADE_DATA_TYPE = new RegistryBuilder<>(TRADE_DATA_TYPE_KEY).create();

        public static final ResourceKey<Registry<TradePriceType<?>>> TRADE_PRICE_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("trade_price_type"));
        public static final Registry<TradePriceType<?>> TRADE_PRICE_TYPE = new RegistryBuilder<>(TRADE_PRICE_TYPE_KEY).sync(true).create();

        public static final ResourceKey<Registry<PermissionType<?>>> PERMISSION_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("trader_permission_type"));
        public static final Registry<PermissionType<?>> PERMISSION_TYPE = new RegistryBuilder<>(PERMISSION_TYPE_KEY).sync(true).create();

        public static final ResourceKey<Registry<Permission<?>>> PERMISSION_KEY = ResourceKey.createRegistryKey(LCApi.id("trader_permission"));
        public static final Registry<Permission<?>> PERMISSION = new RegistryBuilder<>(PERMISSION_KEY).sync(true).create();

    }

    public static final class Upgrades {

        public static final ResourceKey<Registry<UpgradeType>> UPGRADES_KEY = ResourceKey.createRegistryKey(LCApi.id("upgrades"));
        public static final Registry<UpgradeType> UPGRADES = new RegistryBuilder<>(UPGRADES_KEY).sync(true).create();

        public static final ResourceKey<Registry<NumberSourceType<?>>> NUMBER_SOURCE_KEY = ResourceKey.createRegistryKey(LCApi.id("number_source"));
        public static final Registry<NumberSourceType<?>> NUMBER_SOURCE = new RegistryBuilder<>(NUMBER_SOURCE_KEY).sync(true).create();

    }

    public static final class Data {
        private Data() {}

        public static final ResourceKey<Registry<FancyDataType<?>>> FANCY_DATA_KEY = ResourceKey.createRegistryKey(LCApi.id("fancy_data"));
        public static final Registry<FancyDataType<?>> FANCY_DATA = new RegistryBuilder<>(FANCY_DATA_KEY).sync(true).create();

    }

    public static final class Misc {
        private Misc() {}

        public static final ResourceKey<Registry<StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator>>> MENU_VALIDATOR_KEY = ResourceKey.createRegistryKey(LCApi.id("menu_validator"));
        public static final Registry<StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator>> MENU_VALIDATOR = new RegistryBuilder<>(MENU_VALIDATOR_KEY).sync(true).create();

        public static final ResourceKey<Registry<IconType<?>>> ICON_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("icon_type"));
        public static final Registry<IconType<?>> ICON_TYPE = new RegistryBuilder<>(ICON_TYPE_KEY).sync(true).create();

    }

    public static final class Network {
        private Network() {}

        public static final ResourceKey<Registry<FancyPacketType<?>>> PACKET_TYPE_KEY = ResourceKey.createRegistryKey(LCApi.id("packet_type"));
        public static final Registry<FancyPacketType<?>> PACKET_TYPE = new RegistryBuilder<>(PACKET_TYPE_KEY).sync(true).create();

    }

}
