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

public class InfoTab extends TraderStorageTab {

    public static final Identifier KEY = LCApi.id("information");

    public static final TextEntry TOOLTIP = TextEntry.tooltip(LCApi.MODID,"trader.info");

    public InfoTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }
    @Override
    public boolean canOpen() { return this.getPermission(LCPermissions.VIEW_LOGS).hasLowerPermission(); }
    @Override
    public int getTabSortPriority() { return 0; }

    public void assembleAndHandleSettingRequst(TraderNodeType<?> target, FancyPacketMap message) {
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
    public void handleMessage(FancyPacketMap message) {
        if(message.contains("settingRequest"))
            this.handleSettingRequest(message.getMap("settingRequest"));
    }
}
