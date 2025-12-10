package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionList;
import io.github.lightman314.lightmanscurrency.common.menus.TransactionRegisterMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TransactionRegisterItem extends Item {

    public TransactionRegisterItem(Properties properties) { super(properties.stacksTo(1).component(ModDataComponents.REGISTER_TRANSACTIONS.get(),TransactionList.EMPTY)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if(!player.level().isClientSide)
        {
            int itemIndex = -1;
            Inventory inventory = player.getInventory();
            for(int i = 0; i < inventory.getContainerSize() && itemIndex < 0; ++i)
            {
                if(inventory.getItem(i) == heldItem)
                    itemIndex = i;
            }
            if(itemIndex >= 0)
                TransactionRegisterMenu.openMenu(player,itemIndex);
        }
        return InteractionResultHolder.success(heldItem);
    }

}
