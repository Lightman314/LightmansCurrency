package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.InfoTab;

public abstract class InfoClientSubTab extends TraderStorageClientTab<InfoTab> {

    protected InfoClientSubTab(InfoClientTab tab) { super(tab.getMenu(),tab.getCommonTab(),tab.getScreen()); }

    protected final void sendSettingRequest(TraderNodeType<?> node, FancyPacketMap request) {
        this.getCommonTab().assembleAndHandleSettingRequst(node,request);
    }

}