package io.github.lightman314.lightmanscurrency.extendedinventory.container;

import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Mob;

public class ExtendedPlayerContainer extends InventoryMenu
{
    public ExtendedPlayerContainer(Inventory playerInventory, boolean localWorld, Player playerIn)
    {
        super(playerInventory, localWorld, playerIn);
        for(int i = 0; i < ExtendedPlayerInventory.WALLET_INDEXES.size(); i++)
        {
        	this.addSlot(new WalletSlot(playerInventory, ExtendedPlayerInventory.WALLET_INDEXES.get(i), 152, 62 - (18 * i)));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            copy = slotStack.copy();
            EquipmentSlot equipmentslottype = Mob.getEquipmentSlotForItem(copy);
            if(index < 46 && copy.getItem() instanceof WalletItem)
            {
            	if(!this.moveItemStackTo(slotStack, 46, this.slots.size(), false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index == 0)
            {
                if(!this.moveItemStackTo(slotStack, 9, 45, true))
                {
                    return ItemStack.EMPTY;
                }

                //slot.onSlotChange(slotStack, copy);
            }
            else if(index < 5)
            {
                if(!this.moveItemStackTo(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 9)
            {
                if(!this.moveItemStackTo(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(equipmentslottype.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentslottype.getIndex()).hasItem())
            {
                int i = 8 - equipmentslottype.getIndex();
                if(!this.moveItemStackTo(slotStack, i, i + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(equipmentslottype == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem())
            {
                if(!this.moveItemStackTo(slotStack, 45, 46, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index >= 46)
            {
                if(!this.moveItemStackTo(slotStack, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 36)
            {
                if(!this.moveItemStackTo(slotStack, 36, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < 45)
            {
                if(!this.moveItemStackTo(slotStack, 9, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.moveItemStackTo(slotStack, 9, 45, false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if(slotStack.getCount() == copy.getCount())
            {
                return ItemStack.EMPTY;
            }
            
            
            //ItemStack itemstack2 = slot.onTake(playerIn, slotStack);
            //if(index == 0)
            //{
            //    playerIn.dropItem(itemstack2, false);
            //}
        }

        return copy;
    }
    
}
