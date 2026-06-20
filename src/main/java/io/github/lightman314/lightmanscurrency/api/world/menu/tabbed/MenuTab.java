package io.github.lightman314.lightmanscurrency.api.world.menu.tabbed;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

public abstract class MenuTab<T extends TabbedMenu<?,?>> implements ISidedContext {

    private final T menu;
    public final T getMenu() { return this.menu; }
    public final Player getPlayer() { return this.menu.getPlayer(); }

    @Override
    public final boolean isClient() { return this.menu.isClient(); }

    public MenuTab(T menu) { this.menu = menu; }

    public abstract Identifier getClientTabKey();

    public void addMenuSlots(Consumer<Slot> builder) {}

    public void onTabOpened(FancyPacketMap additional) {}
    public void onTabClosed() {}

    public boolean canOpen() { return true; }

    public boolean quickMoveStack(Player player, int slotIndex) { return false; }

    public final void send(FancyPacketMap message) { this.menu.send(message); }
    public final void sendToCient(FancyPacketMap message) { this.menu.sendToClient(message); }
    public final void sendToServer(FancyPacketMap message) { this.menu.sendToServer(message); }
    public void handleMessage(FancyPacketMap packet) {}

}