package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITraderStorageMenu extends IClientTracker, LazyPacketData.IBuilderProvider {

    void setTab(int slot, @Nonnull TraderStorageTab tab);
    void clearTab(int slot);
    void changeTab(int slot);
    void changeTab(int slot, @Nullable LazyPacketData.Builder message);

    @Nonnull
    TradeContext getContext();

    @Nonnull
    RegistryAccess registryAccess();

    @Nonnull
    LazyPacketData.Builder createTabChangeMessage(int newTab);
    @Nonnull
    LazyPacketData.Builder createTabChangeMessage(int newTab, @Nullable LazyPacketData.Builder extraData);

    @Nullable
    TraderData getTrader();
    @Nonnull
    Player getPlayer();

    @Nonnull
    ItemStack getHeldItem();
    void setHeldItem(@Nonnull ItemStack stack);
    void clearContainer(@Nonnull Container container);

    void SetCoinSlotsActive(boolean active);

    void SendMessage(@Nonnull LazyPacketData.Builder message);

    default boolean hasPermission(@Nonnull String permission) { return this.getPermissionLevel(permission) > 0; }
    int getPermissionLevel(@Nonnull String permission);

}
