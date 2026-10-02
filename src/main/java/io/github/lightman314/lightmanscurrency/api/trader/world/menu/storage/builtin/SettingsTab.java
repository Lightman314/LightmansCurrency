package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.resources.Identifier;

public class SettingsTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("settings");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"trader.settings");

    public static final TextEntry CATEGORY_MISC = TextEntry.tooltip(LCApi.MODID,"trader.settings.misc");

    public SettingsTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }

    public void assembleAndHandleSettingRequst(TraderNodeType<?> target,FancyPacketMap message) {
        FancyPacketMap.Mutable m = message.mutable();
        m.setIdentifier("node",target.getKey());
        this.handleSettingRequest(m);
    }

    public void handleSettingRequest(FancyPacketMap message) {
        if(this.isClient())
            this.sendToServer(FancyPacketMap.map().setMap("settingRequest",message));
        ISettingsMessageListener.handleSettingsChange(this,this.getPlayer(),message);
    }

    @Override
    public boolean canOpen() { return this.getPermission(LCPermissions.EDIT_SETTINGS); }

    @Override
    public int getTabSortPriority() { return 5; }

    @Override
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("settingRequest"))
            this.handleSettingRequest(message.getMap("settingRequest"));
    }
}
