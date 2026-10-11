package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.AdminSettingsNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public record SimpleSettingCategory(int sortPriority,IconData icon,Component name,boolean showTitle,Function<IPermissionAccess,Boolean> canAccess) {
    SimpleSettingCategory(IconData icon,Component name,Function<IPermissionAccess,Boolean> canAccess) { this(0,icon,name,false,canAccess); }
    SimpleSettingCategory(IconData icon,Component name) { this(0,icon,name,false,p -> true); }

    public static final SimpleSettingCategory MISC = new SimpleSettingCategory(SpriteIcon.of(LCApi.id("icon/settings")),SettingsTab.CATEGORY_MISC.get());
    public static final SimpleSettingCategory ADMIN = new SimpleSettingCategory(-900,SpriteIcon.of(LCApi.id("icon/creative")),AdminSettingsNode.NAME.get(),true,IPermissionAccess::isAdminPlayer);

}