package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.TransactionRegisterItem;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionList;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class TransactionRegisterMenu extends LazyMessageMenu {

    private final int itemIndex;
    public TransactionRegisterMenu(int id, Inventory inventory, int itemIndex) { this(ModMenus.TRANSACTION_REGISTER.get(),id,inventory,itemIndex); }
    protected TransactionRegisterMenu(MenuType<?> type, int id, Inventory inventory, int itemIndex) {
        super(type, id, inventory);
        this.itemIndex = itemIndex;
        this.addValidator(() -> this.player.getInventory().getItem(this.itemIndex).getItem() instanceof TransactionRegisterItem);
    }

    public TransactionList getData() {
        ItemStack item = this.player.getInventory().getItem(this.itemIndex);
        return item.getOrDefault(ModDataComponents.REGISTER_TRANSACTIONS,TransactionList.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public void HandleMessage(LazyPacketData message) {

    }

    public static void openMenu(Player player, int itemSlot)
    {
        player.openMenu((EasyMenuProvider) (containerId, playerInventory, player1) -> new TransactionRegisterMenu(containerId,playerInventory,itemSlot), b -> b.writeInt(itemSlot));
    }

}
