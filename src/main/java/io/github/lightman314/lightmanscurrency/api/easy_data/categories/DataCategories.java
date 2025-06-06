package io.github.lightman314.lightmanscurrency.api.easy_data.categories;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.easy_data.DataCategory;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;

public class DataCategories {

    public static class Traders
    {

        public static final DataCategory CREATIVE = DataCategory.builder()
                .name(LCText.DATA_CATEGORY_CREATIVE.get()).build();

        public static final DataCategory DISPLAY = DataCategory.builder()
                .permission(Permissions.CHANGE_NAME)
                .name(LCText.DATA_CATEGORY_TRADER_DISPLAY.get())
                .build();

        public static final DataCategory BANK = DataCategory.builder()
                .permission(Permissions.BANK_LINK)
                .name(LCText.DATA_CATEGORY_TRADER_BANK.get())
                .build();


        public static final DataCategory INPUT_SETTINGS = DataCategory.builder()
                .permission(Permissions.InputTrader.EXTERNAL_INPUTS)
                .name(LCText.DATA_CATEGORY_INPUT_SETTINGS.get()).build();

        public static final DataCategory MISC_SETTINGS = DataCategory.builder()
                .permission(Permissions.EDIT_SETTINGS)
                .name(LCText.DATA_CATEGORY_MISC_SETTINGS.get()).build();

    }

}