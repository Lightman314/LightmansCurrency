package io.github.lightman314.lightmanscurrency.api.world.menu.provider;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import javax.annotation.Nullable;

import java.util.function.Consumer;

public record FancyMenuProvider(MenuConstructor constructor, Component name, boolean closeClientMenu,Consumer<RegistryFriendlyByteBuf> packet) implements MenuProvider {

    public FancyMenuProvider(MenuConstructor constructor,Component name,Consumer<RegistryFriendlyByteBuf> packet) { this(constructor,name,true,packet); }
    public FancyMenuProvider(MenuConstructor constructor,Consumer<RegistryFriendlyByteBuf> packet) { this(constructor,Component.empty(),true,packet); }

    @Override
    public Component getDisplayName() { return this.name; }
    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) { return this.constructor.createMenu(containerId,inventory,player); }
    @Override
    public boolean shouldTriggerClientSideContainerClosingOnOpen() { return this.closeClientMenu; }
    @Override
    public void writeClientSideData(AbstractContainerMenu menu,RegistryFriendlyByteBuf buffer) { this.packet.accept(buffer); }

}
