package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.client.tabs.SlotMachinePriceClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SlotMachinePriceTab extends TraderStorageNodeTab<SlotMachineNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("slot_machine_price");

    public SlotMachinePriceTab(ITraderStorageMenu menu) { super(SlotMachineNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public Object createClientTab(Object screen) { return new SlotMachinePriceClientTab(screen, this); }

    @Override
    protected boolean canOpenTab(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    public void SetPrice(MoneyValue newPrice)
    {
        SlotMachineNode node = this.getNode();
        if(this.menu.hasPermission(Permissions.EDIT_TRADES) && node != null)
        {
            node.setPrice(newPrice);
            if(this.menu.isClient())
                this.menu.SendMessage(this.builder().setMoneyValue("SetPrice", newPrice));
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        if(message.contains("SetPrice"))
        {
            this.SetPrice(message.getMoneyValue("SetPrice"));
        }
    }

}
