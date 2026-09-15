package io.github.lightman314.lightmanscurrency.api.text;

import io.github.lightman314.lightmanscurrency.api.LCApi;

import java.util.List;

/**
 * Text constants used in various places within my mod
 */
public final class LCText {

    private static final String MODID = LCApi.MODID;

    private LCText() {}

    public static final TextEntryBundle<Boolean> GUI_SETTINGS_VALUE_TRUE_FALSE = TextEntryBundle.of(List.of(true,false),"gui.lightmanscurrency.settings.value",String::valueOf);

    public static final TextEntry GENERIC_PLURAL = new TextEntry("item.lightmanscurrency.generic.plural");
    public static final TextEntry GENERIC_AND = TextEntry.gui(MODID,"and");
    public static final TextEntry GENERIC_CUSTOM_NAME = TextEntry.gui(MODID,"custom_name");
    public static final TextEntry TOOLTIP_ITEM_COUNT = TextEntry.tooltip(MODID,"item.count");
    public static final ColorBundle VANILLA_COLORS = ColorBundle.of(s -> new TextEntry("color.minecraft." + s));

    public static final class Upgrades {
        private Upgrades() {}

        public static final TextEntry TOOLTIP_UPGRADE_ITEM_CAPACITY = TextEntry.tooltip(MODID,"upgrade.item_capacity");
        public static final TextEntry TOOLTIP_UPGRADE_TRADE_OFFERS = TextEntry.tooltip(MODID,"upgrade.trade_offer");
        public static final TextEntry TOOLTIP_UPGRADE_NETWORK = TextEntry.tooltip(MODID,"upgrade.network");

        public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER = TextEntry.tooltip(MODID,"upgrade.target.traders");
        public static final TextEntry TOOLTIP_UPGRADE_TARGET_TRADER_ITEM = TextEntry.tooltip(MODID,"upgrade.target.traders.item");

    }

    public static final class Resources {
        private Resources() {}

        public static final TextEntry CREATIVE_GROUP_COINS = TextEntry.creativeTab(MODID,"coins");
        public static final TextEntry CREATIVE_GROUP_MACHINES = TextEntry.creativeTab(MODID,"machines");
        public static final TextEntry CREATIVE_GROUP_TRADERS = TextEntry.creativeTab(MODID,"traders");
        public static final TextEntry CREATIVE_GROUP_UPGRADES = TextEntry.creativeTab(MODID,"upgrades");

        public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.title");
        public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_APPLIES_TO = TextEntry.tooltip(MODID,"smithing_template.applies_to");
        public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_INGREDIENTS = TextEntry.tooltip(MODID,"smithing_template.ingredients");
        public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_BASE_SLOT_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.base_slot_description");
        public static final TextEntry TOOLTIP_SMITHING_TEMPLATE_ADDTIONS_SLOT_DESCRIPTION = TextEntry.tooltip(MODID,"smithing_template.additions_slot_description");

        public static final TextEntry KEY_WALLET = TextEntry.keyBind(MODID,"open_wallet");

    }

    @Deprecated(forRemoval = true)
    public static final class Ownership {
        private Ownership() {}

        public static final TextEntry TOOLTIP_OWNER_TEAM = TextEntry.tooltip(MODID,"ownership.team");
        public static final TextEntry TOOLTIP_OWNER_TEAM_FTB = TextEntry.tooltip(MODID,"ownership.team.ftb");

        public static final TextEntry COMMAND_OWNER_LABEL_TEAM = TextEntry.command(MODID,"owner.label.team");

    }

}
