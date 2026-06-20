package io.github.lightman314.lightmanscurrency.api.text;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.MemberLevel;

/**
 * Text constants used in various places within my mod
 */
public final class LCText {

    private static final String MODID = LCApi.MODID;

    private LCText() {}

    public static final TextEntryBundle<Boolean> GUI_SETTINGS_VALUE_TRUE_FALSE = TextEntryBundle.of(ImmutableList.of(true,false),"gui.lightmanscurrency.settings.value",String::valueOf);

    public static final class Config {
        private Config() {}

        public static final TextEntry CONFIG_OPTION_DEFAULT = TextEntry.tooltip(MODID,"config.option.default");
        public static final TextEntry CONFIG_OPTION_RANGE = TextEntry.tooltip(MODID,"config.option.range");
        public static final TextEntry CONFIG_OPTION_OPTIONS = TextEntry.tooltip(MODID,"config.option.options");

    }

    public static final class Money {
        private Money() {}

        public static final TextEntry GUI_MONEY_STORAGE_EMPTY = TextEntry.gui(MODID,"stored_money.empty");

    }

    public static final class Coins {
        private Coins() {}

        public static final TextEntry TOOLTIP_COIN_DISPLAY = TextEntry.tooltip(MODID,"coin.display");
        public static final TextEntry TOOLTIP_COIN_DISPLAY_WORTH = TextEntry.tooltip(MODID,"coin.display.worth");
        public static final TextEntry TOOLTIP_COIN_DISPLAY_NUMBER = TextEntry.tooltip(MODID,"coin.display.number");
        public static final TextEntry TOOLTIP_COIN_DISPLAY_NUMBER_WORDY = TextEntry.tooltip(MODID,"coin.display.number.wordy");
        public static final TextEntry TOOLTIP_COIN_WORTH_DOWN = TextEntry.tooltip(MODID,"coinworth.down");
        public static final TextEntry TOOLTIP_COIN_WORTH_UP = TextEntry.tooltip(MODID,"coinworth.up");
        public static final TextEntry TOOLTIP_COIN_WORTH_VALUE = TextEntry.tooltip(MODID,"coinworth.value");
        public static final TextEntry TOOLTIP_COIN_WORTH_VALUE_STACK = TextEntry.tooltip(MODID,"coinworth.value.stack");
        public static final TextEntry TOOLTIP_COIN_ADVANCED_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.chain");
        public static final TextEntry TOOLTIP_COIN_ADVANCED_VALUE = TextEntry.tooltip(MODID,"coin.advanced.value");
        public static final TextEntry TOOLTIP_COIN_ADVANCED_CORE_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.core_chain");
        public static final TextEntry TOOLTIP_COIN_ADVANCED_SIDE_CHAIN = TextEntry.tooltip(MODID,"coin.advanced.side_chain");

        public static final TextEntry COIN_CHAIN_MAIN = TextEntry.chain(CoinAPI.DEFAULT_CHAIN);
        public static final TextEntry COIN_CHAIN_EMERALDS = TextEntry.chain("emeralds");
        public static final TextEntry COIN_CHAIN_EMERALDS_DISPLAY = TextEntry.chainDisplay("emeralds");
        public static final TextEntry COIN_CHAIN_EMERALDS_DISPLAY_WORDY = TextEntry.chainDisplayWordy("emeralds");

    }

    public static final class Items {
        private Items() {}

        public static final TextEntry TOOLTIP_HEALING = TextEntry.tooltip(MODID,"healing");

        public static final TextEntry TOOLTIP_UPGRADE_TARGETS = TextEntry.tooltip(MODID,"upgrade.targets");
        public static final TextEntry TOOLTIP_UPGRADE_UNIQUE = TextEntry.tooltip(MODID,"upgrade.unique");
        public static final TextEntry TOOLTIP_UPGRADE_ITEM_CAPACITY = TextEntry.tooltip(MODID,"upgrade.item_capacity");

    }

    public static final class Other {
        private Other() {}

        public static final TextEntry CREATIVE_GROUP_COINS = TextEntry.creativeTab(MODID,"coins");
        public static final TextEntry CREATIVE_GROUP_TRADERS = TextEntry.creativeTab(MODID,"traders");

    }

    public static final class Ownership {
        private Ownership() {}

        public static final TextEntryBundle<MemberLevel> BLURB_OWNERSHIP = TextEntryBundle.of(MemberLevel.values(),"blurb.lightmanscurrency.ownership");

        public static final TextEntry GUI_OWNER_NULL = TextEntry.gui(MODID,"owner.null");

        public static final TextEntry TOOLTIP_OWNER_PLAYER = TextEntry.tooltip(MODID,"ownership.player");
        public static final TextEntry TOOLTIP_OWNER_TEAM = TextEntry.tooltip(MODID,"ownership.team");
        public static final TextEntry TOOLTIP_OWNER_TEAM_FTB = TextEntry.tooltip(MODID,"ownership.team.ftb");

        public static final TextEntry COMMAND_OWNER_LABEL_PLAYER = TextEntry.command(MODID,"owner.label.player");
        public static final TextEntry COMMAND_OWNER_LABEL_TEAM = TextEntry.command(MODID,"owner.label.team");
        public static final TextEntry COMMAND_OWNER_LABEL_CUSTOM = TextEntry.command(MODID,"owner.label.custom");

    }

    public static final class Trader {
        public static final TextEntry TOOLTIP_TRADE_EDIT_TAB = TextEntry.tooltip(MODID,"trader.storage.trade_edit");
        public static final TextEntry TOOLTIP_ITEM_STORAGE = TextEntry.tooltip(MODID,"trader.storage.item_storage");
    }

    public static final class Commands {
        private Commands() {}

        //Command Arguments
        public static final TextEntry ARGUMENT_MONEY_VALUE_NOT_A_COIN = TextEntry.argument("money_value.not_a_coin");
        public static final TextEntry ARGUMENT_MONEY_VALUE_DIFFERENT_CHAIN = TextEntry.argument("money_value.different_chain");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_NOT_AN_ANCIENT_COIN = TextEntry.argument("money_value.not_an_ancient_coin");
        public static final TextEntry ARGUMENT_MONEY_VALUE_NO_VALUE = TextEntry.argument("money_value.no_value");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_NOT_EMPTY_OR_FREE = TextEntry.argument("money_value.not_free_or_empty");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_KEY = TextEntry.argument("money_value.impactor.invalid_key");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_CURRENCY = TextEntry.argument("money_value.impactor.invalid_currency");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_IMPACTOR_INVALID_AMOUNT = TextEntry.argument("money_value.impactor.invalid_amount");
        //public static final TextEntry ARGUMENT_MONEY_VALUE_IMPACTOR_ERROR = TextEntry.argument("money_value.impactor.error");
        //public static final TextEntry ARGUMENT_COLOR_INVALID = TextEntry.argument("color.invalid");
        //public static final TextEntry ARGUMENT_TRADEID_INVALID = TextEntry.argument("tradeid.invalid");
        //public static final TextEntry ARGUMENT_TRADER_NOT_FOUND = TextEntry.argument("trader.not_found");
        //public static final TextEntry ARGUMENT_TRADER_NOT_RECOVERABLE = TextEntry.argument("trader.not_recoverable");

    }

    public static final class Misc {
        private Misc() { }

        public static final TextEntry GENERIC_PLURAL = new TextEntry("item.lightmanscurrency.generic.plural");

    }

}
