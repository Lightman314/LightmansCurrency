package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;
import net.minecraft.network.chat.Component;

public record SimpleSettingCategory(int sortPriority,IconData icon,Component name) {
    SimpleSettingCategory(IconData icon,Component name) { this(0,icon,name); }

    public static final SimpleSettingCategory MISC = new SimpleSettingCategory(SpriteIcon.of(LCApi.id("icon/settings")),SettingsTab.CATEGORY_MISC.get());

}