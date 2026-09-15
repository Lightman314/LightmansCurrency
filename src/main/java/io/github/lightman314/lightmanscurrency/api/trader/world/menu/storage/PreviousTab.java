package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.resources.Identifier;

public record PreviousTab(Identifier tabKey,FancyPacketMap data) {
    public PreviousTab(Identifier tabKey) { this(tabKey,FancyPacketMap.EMPTY); }

    public PreviousTab(TraderStorageTab tab) { this(tab.getKey()); }
    public PreviousTab(TraderStorageTab tab,FancyPacketMap data) { this(tab.getKey(),data); }

    public void open(TraderStorageMenu menu) { menu.changeTab(this.tabKey,this.data); }

    public FancyPacketMap asMap() { return FancyPacketMap.map()
            .setIdentifier("tab",tabKey)
            .setOptionalMap("data",this.data);
    }

    public static PreviousTab parseMap(FancyPacketMap data) {
        return new PreviousTab(data.getIdentifier("tab",LCApi.id("null")),data.getMap("data"));
    }

}
