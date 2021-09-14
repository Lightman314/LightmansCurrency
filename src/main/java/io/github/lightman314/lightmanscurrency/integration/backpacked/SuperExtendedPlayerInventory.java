package io.github.lightman314.lightmanscurrency.integration.backpacked;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.util.ItemStackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
//import net.minecraft.entity.player.PlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;

//import com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory;

import java.util.Iterator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SuperExtendedPlayerInventory extends Inventory/*com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory*/ implements IWalletInventory
{
	
    private final NonNullList<ItemStack> walletArray = NonNullList.withSize(1, ItemStack.EMPTY);
    private final NonNullList<ItemStack> walletInventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories = ImmutableList.of(this.items, this.armor, this.offhand, /*this.backpackInventory,*/ this.walletInventory);

    public SuperExtendedPlayerInventory(Player player)
    {
        super(player);
        
        int index = -1;
        for(NonNullList<ItemStack> inventory : this.allInventories)
        {
    		index += inventory.size(); 
        }
        ExtendedPlayerInventory.setWalletIndex(index);
    	LightmansCurrency.LogInfo("(SUPER EXTENDED) Wallet slot index in inventory is " + index);
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
    public ItemStack removeItem(int index, int count)
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

    /*@Override
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
    }*/

    @Override
    public ItemStack removeItemNoUpdate(int index)
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
    public void setItem(int index, ItemStack stack)
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
    public ItemStack getItem(int index)
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
    public ListTag save(ListTag list)
    {
        list = super.save(list);
        for(int i = 0; i < this.walletInventory.size(); i++)
        {
            if(!this.walletInventory.get(i).isEmpty())
            {
                CompoundTag compound = new CompoundTag();
                compound.putByte("Slot", (byte) (i + WRITESLOT));
                this.walletInventory.get(i).save(compound);
                list.add(compound);
            }
        }
        return list;
    }

    @Override
    public void load(ListTag list)
    {
        super.load(list);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag compound = list.getCompound(i);
            int slot = compound.getByte("Slot") & 255;
            ItemStack stack = ItemStack.of(compound);
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
    public int getContainerSize()
    {
        return super.getContainerSize() + this.walletInventory.size();
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
    public boolean contains(ItemStack targetStack)
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
                if(!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, targetStack))
                {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void clearContent()
    {
        for(List<ItemStack> list : this.allInventories)
        {
            list.clear();
        }
    }

    @Override
    public void dropAll()
    {
    	super.dropAll();
    	//Drop the wallet in addition to the vanilla drops
    	for(int i = 0; i < walletInventory.size(); i++)
	    {
    		ItemStack itemstack = walletInventory.get(i);
    		if(!itemstack.isEmpty())
    		{
    			this.player.drop(itemstack, true, false);
    			walletInventory.set(i, ItemStack.EMPTY);
    		}
	    }
    }
}
