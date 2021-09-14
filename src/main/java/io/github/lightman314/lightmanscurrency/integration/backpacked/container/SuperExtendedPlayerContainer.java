package io.github.lightman314.lightmanscurrency.integration.backpacked.container;

import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import com.mrcrayfish.backpacked.inventory.container.ExtendedPlayerContainer;
import com.mrcrayfish.backpacked.item.BackpackItem;

/**
 * Author: MrCrayfish
 */
public class SuperExtendedPlayerContainer extends ExtendedPlayerContainer
{
    public SuperExtendedPlayerContainer(PlayerInventory playerInventory, boolean localWorld, PlayerEntity playerIn)
    {
        super(playerInventory, localWorld, playerIn);
        //CurrencyMod.LOGGER.info("Adding wallet container slot (SUPER EXTENDED).");
        this.addSlot(new WalletSlot(playerInventory, ExtendedPlayerInventory.WALLETINDEX, 152, 62));
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
            copy = slotStack.copy();
            EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(copy);
            if(index != 47 && copy.getItem() instanceof WalletItem)
            {
                if(!this.inventorySlots.get(47).getHasStack())
                {
                    if(!this.mergeItemStack(slotStack, 47, 48, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if(index != 46 && copy.getItem() instanceof BackpackItem) //Backpacked integration
            {
                if(!this.inventorySlots.get(46).getHasStack())
                {
                    if(!this.mergeItemStack(slotStack, 46, 47, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if(index == 0)
            {
                if(!this.mergeItemStack(slotStack, 9, 45, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(slotStack, copy);
            }
            else if(index < 5)
            {
                if(!this.mergeItemStack(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 9)
            {
                if(!this.mergeItemStack(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(8 - equipmentslottype.getIndex()).getHasStack())
            {
                int i = 8 - equipmentslottype.getIndex();
                if(!this.mergeItemStack(slotStack, i, i + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(45).getHasStack())
            {
                if(!this.mergeItemStack(slotStack, 45, 46, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index == 46)
            {
                if(!this.mergeItemStack(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 36)
            {
                if(!this.mergeItemStack(slotStack, 36, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 45)
            {
                if(!this.mergeItemStack(slotStack, 9, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.mergeItemStack(slotStack, 9, 45, false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if(slotStack.getCount() == copy.getCount())
            {
                return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = slot.onTake(playerIn, slotStack);
            if(index == 0)
            {
                playerIn.dropItem(itemstack2, false);
            }
        }

        return copy;
    }
    
}
