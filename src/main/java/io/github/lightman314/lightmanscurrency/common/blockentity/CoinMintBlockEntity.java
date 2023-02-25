package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class CoinMintBlockEntity extends EasyBlockEntity {

	SimpleContainer storage = new SimpleContainer(2);
	public SimpleContainer getStorage() { return this.storage; }
	
	private final MintItemCapability itemHandler = new MintItemCapability(this);
	private final LazyOptional<IItemHandler> inventoryHandlerLazyOptional = LazyOptional.of(() -> this.itemHandler);
	
	private List<CoinMintRecipe> getCoinMintRecipes()
	{
		if(this.level != null)
			return getCoinMintRecipes(this.level);
		return Lists.newArrayList();
	}
	
	public static List<CoinMintRecipe> getCoinMintRecipes(Level level) { return RecipeValidator.getValidRecipes(level).getCoinMintRecipes(); }
	
	public CoinMintBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COIN_MINT.get(), pos, state); }
	
	protected CoinMintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.storage.addListener(container -> this.setChanged());
	}
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.storage);
		super.saveAdditional(compound);
	}
	
	@Override
	public void load(@NotNull CompoundTag compound)
	{
		super.load(compound);
		
		this.storage = InventoryUtil.loadAllItems("Storage", compound, 2);
		this.storage.addListener(container -> this.setChanged());
		
	}
	
	public void dumpContents(Level world, BlockPos pos)
	{
		InventoryUtil.dumpContents(world, pos, this.storage);
	}
	
	//Coin Minting Functions
	public boolean validMintInput() { return this.getRelevantRecipe() != null; }
	
	public boolean validMintInput(ItemStack item)
	{
		Container tempInv = new SimpleContainer(1);
		tempInv.setItem(0, item);
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(tempInv, this.level))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the amount of available empty space the output slot has.
	 * Returns 0 if the mint input does not create the same item currently in the output slot.
	 */
	public int validOutputSpace()
	{
		//Determine how many more coins can fit in the output slot based on the input item
		CoinMintRecipe recipe = this.getRelevantRecipe();
		if(recipe == null)
			return 0;
		ItemStack mintOutput = recipe.getResultItem();
		ItemStack currentOutputSlot = this.getStorage().getItem(1);
		if(currentOutputSlot.isEmpty())
			return mintOutput.getMaxStackSize();
		else if(!InventoryUtil.ItemMatches(currentOutputSlot, mintOutput))
			return 0;
		return currentOutputSlot.getMaxStackSize() - currentOutputSlot.getCount();
	}

	@Nullable
	public CoinMintRecipe getRelevantRecipe()
	{
		ItemStack mintInput = this.getStorage().getItem(0);
		if(mintInput.isEmpty())
			return null;
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(this.storage, this.level))
				return recipe;
		}
		return null;
	}
	
	/**
	 * Returns the maximum result item stack that can fit into the output slots.
	 */
	public ItemStack getMintableOutput() {
		CoinMintRecipe recipe = this.getRelevantRecipe();
		if(recipe == null)
			return ItemStack.EMPTY;
		ItemStack output = recipe.getResultItem();
		int countPerMint = output.getCount();
		int outputSpace = validOutputSpace();
		//Shrink by 1, as the first input item is consumed in the starting output item count
		int inputCount = this.storage.getItem(0).getCount() - recipe.ingredientCount;
		while(output.getCount() + countPerMint <= outputSpace && inputCount > 0)
		{
			output.grow(countPerMint);
			inputCount -= recipe.ingredientCount;
		}
		return output;
	}
	
	public void mintCoins(int mintCount)
	{
		CoinMintRecipe recipe = this.getRelevantRecipe();
		if(recipe == null)
			return;
		ItemStack mintOutput = recipe.getResultItem();
		//Ignore if no valid input is present
		if(mintOutput.isEmpty())
			return;

		// Since "mintCount" is the number of output items requested, divide it by the output count, and round up
		// such that it is properly converted from "requested amount" variable to a "craft iteration" variable.
		mintCount = (int)Math.ceil((double) mintCount / mintOutput.getCount());

		//Determine how many to mint based on the input count & whether a fullStack input was given.
		if(mintCount > this.getStorage().getItem(0).getCount() / recipe.ingredientCount)
			mintCount = this.getStorage().getItem(0).getCount() / recipe.ingredientCount;
		
		//Confirm that the output slot has enough room for the expected outputs
		if(mintCount * mintOutput.getCount() > validOutputSpace())
			mintCount = validOutputSpace() / mintOutput.getCount();
		if(mintCount <= 0)
			return;
		
		//Get the output items
		mintOutput.setCount(mintCount * mintOutput.getCount());
		
		//Place the output item(s)
		if(this.getStorage().getItem(1).isEmpty())
		{
			this.getStorage().setItem(1, mintOutput);
		}
		else
		{
			this.getStorage().getItem(1).grow(mintOutput.getCount());
		}
		
		//Remove the input item(s)
		this.getStorage().removeItem(0, mintCount * recipe.ingredientCount);
		
		//Job is done!
		this.setChanged();
		
	}
	
	//Client Synchronization
	@Override
	public void onLoad() {
		if(this.level.isClientSide)
			BlockEntityUtil.requestUpdatePacket(this);
	}
	
	//Item capability for hopper and item automation
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side)
	{
		if(cap == ForgeCapabilities.ITEM_HANDLER)
			return this.inventoryHandlerLazyOptional.cast();
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

		final CoinMintBlockEntity mint;
		public MintItemCapability(CoinMintBlockEntity tileEntity) { this.mint = tileEntity; }
		
		@Override
		public int getSlots() {
			return this.mint.getStorage().getContainerSize();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			if(slot == 1)
			{
				if(this.mint.getStorage().getItem(1).isEmpty() && this.mint.getRelevantRecipe() != null)
				{
					//Simulate minted amount if the output slot is not currently empty.
					return this.mint.getMintableOutput();
				}
			}
			return this.mint.getStorage().getItem(slot);
		}
		
		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			//Can only insert into slot 0
			if(slot != 0)
				return stack.copy();
			if(!this.mint.validMintInput(stack))
				return stack.copy();
			//Confirm that the input slot is valid
			ItemStack currentStack = this.mint.getStorage().getItem(0);
			if(currentStack.isEmpty())
			{
				if(stack.getCount() > stack.getMaxStackSize())
				{
					//Move the item into storage, and return the leftovers
					if(!simulate)
					{
						ItemStack placeStack = stack.copy();
						placeStack.setCount(stack.getMaxStackSize());
						this.mint.getStorage().setItem(0, placeStack);
					}
					ItemStack leftoverStack = stack.copy();
					leftoverStack .setCount(stack.getCount() - stack.getMaxStackSize());
					return leftoverStack;
				}
				else
				{
					//Move the item into storage and return an empty stack
					if(!simulate)
						this.mint.getStorage().setItem(0, stack.copy());
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
					this.mint.getStorage().setItem(0, newStack);
				}
				ItemStack leftoverStack = stack.copy();
				leftoverStack.setCount(stack.getCount() + currentStack.getCount() - newAmount);
				return leftoverStack;
			}
			//Slot is already full of another item type
			return stack.copy();
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			//Can only extract from slot 1
			if(slot != 1)
				return ItemStack.EMPTY;
			
			//LightmansCurrency.LogInfo("Attempting to extract " + amount + " items from the coin mint.");
			//Limit request amount to 1 stack
			amount = MathUtil.clamp(amount, 0, 64);
			//Copy so that the simulation doesn't cause problems
			ItemStack currentStack = this.mint.getStorage().getItem(1).copy();
			//LightmansCurrency.LogInfo("Starting output items: " + currentStack.getCount());
			if(currentStack.isEmpty() || currentStack.getCount() < amount)
			{
				//Attempt to mint coins to fill the extra pull requests
				int mintAmount = Math.min(this.mint.getMintableOutput().getCount(), amount - currentStack.getCount());
				if(!simulate)
				{
					if(mintAmount > 0) //Mint the coins
					{
						//Mint the coins
						this.mint.mintCoins(mintAmount);
						//Update the output stack now that the coins have been minted
						currentStack = this.mint.getStorage().getItem(1).copy();
					}
				}
				else if(mintAmount > 0)
				{
					//Emulate the minting
					if(currentStack.isEmpty())
						currentStack = this.mint.getMintableOutput();
					else
						currentStack.grow(mintAmount);
				}
			}
			
			//No items to output even after attempting to mint
			if(currentStack.isEmpty())
				return ItemStack.EMPTY;
			
			ItemStack outputStack = currentStack.copy();
			//Get the output stack
			if(outputStack.getCount() > amount)
				outputStack.setCount(amount);
			//Remove the output coins from the current stack
			if(!simulate)
			{
				currentStack.setCount(currentStack.getCount() - outputStack.getCount());
				if(currentStack.getCount() <= 0)
					currentStack = ItemStack.EMPTY;
				this.mint.getStorage().setItem(1, currentStack);
			}
			
			
			//Return the output stack
			return outputStack;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot == 0 && this.mint.validMintInput(stack); }
		
	}
	
}
