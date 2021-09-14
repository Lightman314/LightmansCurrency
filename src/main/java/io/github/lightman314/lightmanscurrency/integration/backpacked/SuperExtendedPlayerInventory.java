package io.github.lightman314.lightmanscurrency.integration.backpacked;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

//import com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory;

import java.util.Iterator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SuperExtendedPlayerInventory extends com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory implements IWalletInventory
{
	
    private final NonNullList<ItemStack> walletArray = NonNullList.withSize(1, ItemStack.EMPTY);
    private final NonNullList<ItemStack> walletInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories = ImmutableList.of(this.mainInventory, this.armorInventory, this.offHandInventory, this.backpackInventory, this.walletInventory);

    public SuperExtendedPlayerInventory(PlayerEntity player)
    {
        super(player);
        
        if(ExtendedPlayerInventory.WALLETINDEX < 0)
        {
        	for(NonNullList<ItemStack> inventory : this.allInventories)
            {
        		ExtendedPlayerInventory.WALLETINDEX += inventory.size(); 
            }
        	LightmansCurrency.LogInfo("(SUPER EXTENDED) Wallet slot index in inventory is " + ExtendedPlayerInventory.WALLETINDEX);
        }
    }
    
    public NonNullList<ItemStack> getWalletArray()
    {
    	return walletArray;
    }

    public NonNullList<ItemStack> getWalletItems()
    {
        return walletInventory;
    }

    public void copyWallet(IWalletInventory inventory)
    {
        this.walletInventory.set(0, inventory.getWalletItems().get(0));
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        NonNullList<ItemStack> targetInventory = null;
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            if(index < inventory.size())
            {
                targetInventory = inventory;
                break;
            }
            index -= inventory.size();
        }
        return targetInventory != null && !targetInventory.get(index).isEmpty() ? ItemStackHelper.getAndSplit(targetInventory, index, count) : ItemStack.EMPTY;
    }

    @Override
    public void deleteStack(ItemStack stack)
    {
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            for(int i = 0; i < inventory.size(); ++i)
            {
                if(inventory.get(i) == stack)
                {
                    inventory.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        NonNullList<ItemStack> targetInventory = null;
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            if(index < inventory.size())
            {
                targetInventory = inventory;
                break;
            }
            index -= inventory.size();
        }

        if(targetInventory != null && !targetInventory.get(index).isEmpty())
        {
            ItemStack stack = targetInventory.get(index);
            targetInventory.set(index, ItemStack.EMPTY);
            return stack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        NonNullList<ItemStack> targetInventory = null;
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            if(index < inventory.size())
            {
                targetInventory = inventory;
                break;
            }
            index -= inventory.size();
        }
        if(targetInventory != null)
        {
            targetInventory.set(index, stack);
        }
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        List<ItemStack> list = null;
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            if(index < inventory.size())
            {
                list = inventory;
                break;
            }
            index -= inventory.size();
        }
        return list == null ? ItemStack.EMPTY : list.get(index);
    }
    
    private final int WRITESLOT = 213;
    
    @Override
    public ListNBT write(ListNBT list)
    {
        list = super.write(list);
        for(int i = 0; i < this.walletInventory.size(); i++)
        {
            if(!this.walletInventory.get(i).isEmpty())
            {
                CompoundNBT compound = new CompoundNBT();
                compound.putByte("Slot", (byte) (i + WRITESLOT));
                this.walletInventory.get(i).write(compound);
                list.add(compound);
            }
        }
        return list;
    }

    @Override
    public void read(ListNBT list)
    {
        super.read(list);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundNBT compound = list.getCompound(i);
            int slot = compound.getByte("Slot") & 255;
            ItemStack stack = ItemStack.read(compound);
            if(!stack.isEmpty())
            {
                if(slot >= WRITESLOT && slot < this.walletInventory.size() + WRITESLOT)
                {
                    this.walletInventory.set(slot - WRITESLOT, stack);
                }
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return super.getSizeInventory() + this.walletInventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.walletInventory)
        {
            if(!stack.isEmpty())
            {
                return false;
            }
        }
        return super.isEmpty();
    }

    @Override
    public boolean hasItemStack(ItemStack targetStack)
    {
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
            Iterator<ItemStack> iterator = inventory.iterator();
            while(true)
            {
                if(!iterator.hasNext())
                {
                    return false;
                }
                ItemStack stack = (ItemStack) iterator.next();
                if(!stack.isEmpty() && stack.isItemEqual(targetStack))
                {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear()
    {
        for(List<ItemStack> list : this.allInventories)
        {
            list.clear();
        }
    }

    @Override
    public void dropAllItems()
    {
    	super.dropAllItems();
    	//Drop the wallet in addition to the vanilla drops
    	for(int i = 0; i < walletInventory.size(); i++)
	    {
    		ItemStack itemstack = walletInventory.get(i);
    		if(!itemstack.isEmpty())
    		{
    			this.player.dropItem(itemstack, true, false);
    			walletInventory.set(i, ItemStack.EMPTY);
    		}
	    }
    }
}
