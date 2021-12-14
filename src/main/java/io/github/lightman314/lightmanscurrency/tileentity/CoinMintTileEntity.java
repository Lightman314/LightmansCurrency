package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber
public class CoinMintTileEntity extends TileEntity{

	IInventory storage = new Inventory(2);
	public IInventory getStorage() { return this.storage; }
	
	private final LazyOptional<IItemHandler> inventoryHandlerLazyOptional = LazyOptional.of(() -> new MintItemCapability(this));
	
	private final List<CoinMintRecipe> getCoinMintRecipes()
	{
		if(this.world != null)
			return RecipeValidator.getValidRecipes(this.world).getCoinMintRecipes();
		return Lists.newArrayList();
	}
	
	public CoinMintTileEntity() {
		super(ModTileEntities.COIN_MINT);
	}
	
	protected CoinMintTileEntity(TileEntityType<?> type)
	{
		super(type);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.storage);
		return super.write(compound);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		super.read(state, compound);
		
		this.storage = InventoryUtil.loadAllItems("Storage", compound, 2);
		
	}
	
	public void dumpContents(World world, BlockPos pos)
	{
		InventoryUtil.dumpContents(world, pos, this.storage);
	}
	
	//Coin Minting Functions
	public boolean validMintInput()
	{
		return !getMintOutput().isEmpty();
	}
	
	public boolean validMintInput(ItemStack item)
	{
		IInventory tempInv = new Inventory(1);
		tempInv.setInventorySlotContents(0, item);
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(tempInv, this.world))
			{
				return true;
			}
		}
		return false;
	}
	
	public int validMintOutput()
	{
		//Determine how many more coins can fit in the output slot based on the input item
		ItemStack mintOutput = getMintOutput();
		ItemStack currentOutputSlot = this.getStorage().getStackInSlot(1);
		if(currentOutputSlot.isEmpty())
			return 64;
		else if(currentOutputSlot.getItem() != mintOutput.getItem())
			return 0;
		return currentOutputSlot.getMaxStackSize() - currentOutputSlot.getCount();
	}
	
	public ItemStack getMintOutput()
	{
		ItemStack mintInput = this.getStorage().getStackInSlot(0);
		if(mintInput.isEmpty())
			return ItemStack.EMPTY;
		
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(this.storage, this.world))
			{
				return recipe.getCraftingResult(this.storage);
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	public void mintCoins(int mintCount)
	{
		//Ignore if no valid input is present
		if(!validMintInput())
			return;
		
		//Determine how many to mint based on the input count & whether a fullStack input was given.
		if(mintCount > this.getStorage().getStackInSlot(0).getCount())
		{
			mintCount = this.getStorage().getStackInSlot(0).getCount();
		}
		
		//Confirm that the output slot has enough room for the expected outputs
		if(mintCount > validMintOutput())
			mintCount = validMintOutput();
		if(mintCount <= 0)
			return;
		
		//Get the output items
		ItemStack mintOutput = getMintOutput();
		mintOutput.setCount(mintCount);
		
		//Place the output item(s)
		if(this.getStorage().getStackInSlot(1).isEmpty())
		{
			this.getStorage().setInventorySlotContents(1, mintOutput);
		}
		else
		{
			this.getStorage().getStackInSlot(1).setCount(this.getStorage().getStackInSlot(1).getCount() + mintOutput.getCount());
		}
		
		//Remove the input item(s)
		this.getStorage().getStackInSlot(0).setCount(this.getStorage().getStackInSlot(0).getCount() - mintCount);
		
		//Job is done!
		this.markDirty();
		
	}
	
	//Client Synchronization
	@Override
	public void onLoad() {
		if(this.world.isRemote)
			TileEntityUtil.requestUpdatePacket(this.world, this.pos);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, -1, this.write(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		this.read(this.getBlockState(), pkt.getNbtCompound());
	}
	
	//Item capability for hopper and item automation
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return inventoryHandlerLazyOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		inventoryHandlerLazyOptional.invalidate();
	}
	
	public static class MintItemCapability implements IItemHandler
	{

		final CoinMintTileEntity tileEntity;
		public MintItemCapability(CoinMintTileEntity tileEntity) { this.tileEntity = tileEntity; }
		
		@Override
		public int getSlots() {
			return this.tileEntity.getStorage().getSizeInventory();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.tileEntity.getStorage().getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			//Can only insert into slot 0
			if(slot != 0)
				return stack.copy();
			if(!this.tileEntity.validMintInput(stack))
				return stack.copy();
			//Confirm that the input slot is valid
			ItemStack currentStack = this.tileEntity.getStorage().getStackInSlot(0);
			if(currentStack.isEmpty())
			{
				if(stack.getCount() > stack.getMaxStackSize())
				{
					//Move the item into storage, and return the leftovers
					if(!simulate)
					{
						ItemStack placeStack = stack.copy();
						placeStack.setCount(stack.getMaxStackSize());
						this.tileEntity.getStorage().setInventorySlotContents(0, placeStack);
					}
					ItemStack leftoverStack = stack.copy();
					leftoverStack .setCount(stack.getCount() - stack.getMaxStackSize());
					return leftoverStack;
				}
				else
				{
					//Move the item into storage and return an empty stack
					if(!simulate)
						this.tileEntity.getStorage().setInventorySlotContents(0, stack.copy());
					return ItemStack.EMPTY;
				}
				
			}
			else if(InventoryUtil.ItemMatches(currentStack, stack))
			{
				int newAmount = MathUtil.clamp(currentStack.getCount() + stack.getCount(), 0, currentStack.getMaxStackSize());
				if(!simulate)
				{
					ItemStack newStack = currentStack.copy();
					newStack.setCount(newAmount);
					this.tileEntity.getStorage().setInventorySlotContents(0, newStack);
				}
				ItemStack leftoverStack = stack.copy();
				leftoverStack.setCount(stack.getCount() + currentStack.getCount() - newAmount);
				return leftoverStack;
			}
			//Slot is already full of another item type
			return stack.copy();
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			//Can only extract from slot 1
			if(slot != 1)
				return ItemStack.EMPTY;
			//Limit request amount to 1 stack
			amount = MathUtil.clamp(amount, 0, 64);
			//Copy so that the simulation doesn't cause problems
			ItemStack currentStack = this.tileEntity.getStorage().getStackInSlot(1).copy();
			if(currentStack.isEmpty() || currentStack.getCount() < amount)
			{
				//Attempt to mint coins to fill the extra pull requests
				int mintAmount = MathUtil.clamp(amount - currentStack.getCount(), 0, this.tileEntity.validMintOutput());
				if(!simulate)
				{
					if(mintAmount > 0) //Mint the coins
					{
						//Mint the coins
						this.tileEntity.mintCoins(mintAmount);
						//Update the output stack now that the coins have been minted
						currentStack = this.tileEntity.getStorage().getStackInSlot(1).copy();
					}
				}
				else if(mintAmount > 0)
				{
					//Emulate the minting
					if(currentStack.isEmpty())
					{
						currentStack = this.tileEntity.getMintOutput();
						currentStack.setCount(mintAmount);
					}
					else
						currentStack.grow(mintAmount);
				}
			}
			
			//No items to output even after attempting to mint
			if(currentStack.isEmpty())
				return ItemStack.EMPTY;
			
			
			ItemStack outputStack = currentStack.copy();
			//Get the output stack
			outputStack.setCount(MathUtil.clamp(amount, 0, currentStack.getCount()));
			//Remove the output coins from the current stack
			currentStack.setCount(currentStack.getCount() - outputStack.getCount());
			if(currentStack.getCount() <= 0)
				currentStack = ItemStack.EMPTY;
			
			//Set the new current stack count
			if(!simulate)
				this.tileEntity.getStorage().setInventorySlotContents(1, currentStack);
			
			//Return the output stack
			return outputStack;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 ? this.tileEntity.validMintInput(stack) : false;
		}
		
	}
	
}
