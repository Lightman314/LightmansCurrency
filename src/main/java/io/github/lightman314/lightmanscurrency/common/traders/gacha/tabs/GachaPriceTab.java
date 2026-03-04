package io.github.lightman314.lightmanscurrency.common.traders.gacha.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.client.tabs.GachaPriceClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class GachaPriceTab extends TraderStorageNodeTab<GachaNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("gacha_price");

    public GachaPriceTab(ITraderStorageMenu menu) { super(GachaNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new GachaPriceClientTab(screen,this); }

    @Override
    public boolean canOpenTab(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    public void setPrice(MoneyValue price) {
        GachaNode node = this.getNode();
        if(node != null)
        {
            node.setPrice(this.menu.getPlayer(),price);
            if(this.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("ChangePrice",price));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("ChangePrice"))
            this.setPrice(message.getMoneyValue("ChangePrice"));
    }

}
