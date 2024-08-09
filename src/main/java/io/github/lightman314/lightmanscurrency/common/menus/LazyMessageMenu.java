package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.network.message.menu.*;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nonnull;

public abstract class LazyMessageMenu extends EasyMenu {

    protected LazyMessageMenu(MenuType<?> type, int id, Inventory inventory) { super(type, id, inventory); }
    protected LazyMessageMenu(MenuType<?> type, int id, Inventory inventory, MenuValidator validator) { super(type, id, inventory, validator); }

    @Nonnull
    public final LazyPacketData.Builder builder() { return LazyPacketData.builder(); }

    public void SendMessage(@Nonnull LazyPacketData.Builder message)
    {
        if(this.isClient())
            this.SendMessageToServer(message);
        else
            this.SendMessageToClient(message);
    }

    public void SendMessageToServer(@Nonnull LazyPacketData.Builder message)
    {
        if(this.isServer())
            return;
        new CPacketLazyMenu(this.containerId,message).send();
    }

    public void SendMessageToClient(@Nonnull LazyPacketData.Builder message)
    {
        if(this.isClient())
            return;
        new SPacketLazyMenu(this.containerId,message).sendTo(this.player);
    }

    public abstract void HandleMessage(@Nonnull LazyPacketData message);

}
