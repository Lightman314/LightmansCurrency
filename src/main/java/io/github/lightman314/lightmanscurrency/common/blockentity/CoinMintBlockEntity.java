package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoinMintBlockEntity extends EasyBlockEntity implements IServerTicker {

	SimpleContainer storage = new SimpleContainer(2);
	public Container getStorage() { return this.storage; }

	public SingleRecipeInput getRecipeInput() { return new SingleRecipeInput(this.storage.getItem(0)); }

	private CoinMintRecipe lastRelevantRecipe = null;
	private int mintTime = 0;
	public int getMintTime() { return this.mintTime; }
	public float getMintProgress() { return (float)this.mintTime/(float)this.getExpectedMintTime(); }
	public int getExpectedMintTime() { if(this.lastRelevantRecipe != null) return this.lastRelevantRecipe.getDuration(); return -1; }
	
	private final MintItemCapability itemHandler = new MintItemCapability(this);
	public IItemHandler getItemHandler() { return this.itemHandler; }

	@Nonnull
	private List<CoinMintRecipe> getCoinMintRecipes()
	{
		if(this.level != null)
			return getCoinMintRecipes(this.level);
		return Lists.newArrayList();
	}

	@Nonnull
	public static List<CoinMintRecipe> getCoinMintRecipes(Level level) { return RecipeValidator.getValidMintRecipes(level); }
	
	public CoinMintBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COIN_MINT.get(), pos, state); }
	
	protected CoinMintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.storage.addListener(this::onInventoryChanged);
	}
	
	@Override
	public void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.storage, lookup);
		compound.putInt("MintTime", this.mintTime);
		super.saveAdditional(compound,lookup);
	}
	
	@Override
	public void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		super.loadAdditional(compound, lookup);

		if(compound.contains("Storage"))
		{
			this.storage = InventoryUtil.loadAllItems("Storage", compound, 2, lookup);
			this.storage.addListener(this::onInventoryChanged);
		}

		if(compound.contains("MintTime"))
			this.mintTime = compound.getInt("MintTime");

	}

	@Override
	public void onLoad() {
		if(this.level.isClientSide)
			BlockEntityUtil.requestUpdatePacket(this);
		this.lastRelevantRecipe = this.getRelevantRecipe();
	}

	private void onInventoryChanged(Container inventory)
	{
		if(inventory != this.storage)
			return;
		this.setChanged();
		this.checkRecipes();
	}

	public void checkRecipes()
	{
		CoinMintRecipe newRecipe = this.getRelevantRecipe();
		if(this.lastRelevantRecipe != newRecipe)
		{
			this.lastRelevantRecipe = newRecipe;
			this.mintTime = 0;
			this.markMintTimeDirty();
		}
	}

	@Override
	public void serverTick() {
		if(this.lastRelevantRecipe != null && this.storage.getItem(0).getCount() >= this.lastRelevantRecipe.ingredientCount && this.hasOutputSpace())
		{
			this.mintTime++;
			if(this.mintTime >= this.lastRelevantRecipe.getDuration())
			{
				this.mintTime = 0;
				this.mintCoin();
				float volume = LCConfig.SERVER.coinMintSoundVolume.get();
				if(volume > 0f)
					this.level.playSound(null, this.worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, volume, 1f);
			}
			this.markMintTimeDirty();
		}
		else if(this.mintTime > 0)
		{
			this.mintTime = 0;
			this.markMintTimeDirty();
		}
	}

	private void markMintTimeDirty()
	{
		this.setChanged();
		CompoundTag updateTag = new CompoundTag();
		updateTag.putInt("MintTime", this.mintTime);
		BlockEntityUtil.sendUpdatePacket(this, updateTag);
	}

	public void dumpContents(Level world, BlockPos pos) { InventoryUtil.dumpContents(world, pos, this.storage); }
	
	//Coin Minting Functions
	public boolean validMintInput(@Nonnull ItemStack item)
	{
		SingleRecipeInput temp = new SingleRecipeInput(item);
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(temp, this.level))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the amount of available empty space the output slot has.
	 * Returns 0 if the mint input does not create the same item currently in the output slot.
	 */
	public boolean hasOutputSpace()
	{
		//Determine how many more coins can fit in the output slot based on the input item
		if(this.lastRelevantRecipe == null)
			return false;
		ItemStack mintOutput = this.lastRelevantRecipe.getResultItem(this.level.registryAccess());
		ItemStack currentOutputSlot = this.getStorage().getItem(1);
		if(currentOutputSlot.isEmpty())
			return true;
		else if(!InventoryUtil.ItemMatches(currentOutputSlot, mintOutput))
			return false;
		return currentOutputSlot.getMaxStackSize() - currentOutputSlot.getCount() >= this.lastRelevantRecipe.getOutputItem().getCount();
	}

	@Nullable
	public CoinMintRecipe getRelevantRecipe()
	{
		ItemStack mintInput = this.getStorage().getItem(0);
		if(mintInput.isEmpty())
			return null;
		SingleRecipeInput input = this.getRecipeInput();
		for(CoinMintRecipe recipe : this.getCoinMintRecipes())
		{
			if(recipe.matches(input, this.level))
				return recipe;
		}
		return null;
	}
	
	public void mintCoin()
	{
		this.lastRelevantRecipe = this.getRelevantRecipe();
		if(this.lastRelevantRecipe == null)
			return;
		ItemStack mintOutput = this.lastRelevantRecipe.getResultItem(this.level.registryAccess());
		//Ignore if no valid input is present
		if(mintOutput.isEmpty())
			return;
		
		//Confirm that the output slot has enough room for the expected outputs
		if(!this.hasOutputSpace())
			return;

		//Confirm that we have the required inputs
		if(this.storage.getItem(0).getCount() < this.lastRelevantRecipe.ingredientCount)
			return;
		
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
		this.getStorage().removeItem(0, mintOutput.getCount());
		
		//Job is done!
		this.setChanged();
		
	}

	public static class MintItemCapability implements IItemHandler
	{

		final CoinMintBlockEntity mint;
		public MintItemCapability(CoinMintBlockEntity tileEntity) { this.mint = tileEntity; }
		
		@Override
		public int getSlots() { return this.mint.getStorage().getContainerSize(); }

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) { return this.mint.getStorage().getItem(slot); }
		
		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
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
					stack = stack.copy();
					ItemStack placeStack = stack.split(stack.getMaxStackSize());
					if(!simulate)
						this.mint.getStorage().setItem(0, placeStack);
					return stack;
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

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			//Can only extract from slot 1
			if(slot != 1)
				return ItemStack.EMPTY;
			
			//LightmansCurrency.LogInfo("Attempting to extract " + amount + " items from the coin mint.");
			//Limit request amount to 1 stack
			amount = MathUtil.clamp(amount, 0, 64);
			//Copy so that the simulation doesn't cause problems
			ItemStack currentStack = this.mint.getStorage().getItem(1).copy();
			
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
		public int getSlotLimit(int slot) { return 64; }

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot == 0 && this.mint.validMintInput(stack); }
		
	}
	
}
