package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CoinManagementMenu extends LazyMessageMenu {


    public CoinManagementMenu(int id, @Nonnull Inventory inventory) { super(ModMenus.COIN_MANAGEMENT.get(), id, inventory); }



    @Override
    public void HandleMessage(@Nonnull LazyPacketData message) {

    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) { return ItemStack.EMPTY; }
}