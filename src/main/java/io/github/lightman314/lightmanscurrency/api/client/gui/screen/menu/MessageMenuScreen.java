package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class MessageMenuScreen<T extends MessageMenu> extends FancyMenuScreen<T> {


    public MessageMenuScreen(T menu, Inventory inventory) { super(menu, inventory); this.menu.addClientListener(this::handleMessage); }
    public MessageMenuScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title); this.menu.addClientListener(this::handleMessage); }
    public MessageMenuScreen(T menu, Inventory inventory, int width, int height) { super(menu, inventory, width, height); this.menu.addClientListener(this::handleMessage); }
    public MessageMenuScreen(T menu, Inventory inventory, Component title, int width, int height) { super(menu, inventory, title, width, height); this.menu.addClientListener(this::handleMessage); }

    public final void sendMessage(FancyPacketMap message) { this.menu.sendToServer(message); }

    protected void handleMessage(FancyPacketMap message) {}

}