package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.network.message.menu.CPacketLazyMenu;
import io.github.lightman314.lightmanscurrency.network.message.menu.SPacketLazyMenu;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class LazyMessageMenu extends EasyMenu {

    public final Player player;

    protected LazyMessageMenu(MenuType<?> type, int id, Inventory inventory) { super(type, id, inventory); this.player = inventory.player; }
    protected LazyMessageMenu(MenuType<?> type, int id, Inventory inventory, MenuValidator validator) { super(type, id, inventory, validator); this.player = inventory.player; }

    public void SendMessage(LazyPacketData.Builder message)
    {
        if(this.isClient())
            this.SendMessageToServer(message);
        else
            this.SendMessageToClient(message);
    }

    public void SendMessageToServer(LazyPacketData.Builder message)
    {
        if(this.isServer())
            return;
        new CPacketLazyMenu(message).send();
    }

    public void SendMessageToClient(LazyPacketData.Builder message)
    {
        if(this.isClient())
            return;
        new SPacketLazyMenu(message).sendTo(this.player);
    }

    public abstract void HandleMessage(LazyPacketData message);

}