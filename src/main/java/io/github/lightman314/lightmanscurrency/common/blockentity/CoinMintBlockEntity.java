package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.misc.ticker.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class CoinMintBlockEntity extends EasyBlockEntity implements IServerTicker {

	private final CoinMintInventory storage = new CoinMintInventory();
	public IItemHandlerModifiable getStorage() { return this.storage; }

	public SingleRecipeInput getRecipeInput() { return new SingleRecipeInput(this.storage.getStackInSlot(0)); }

	private CoinMintRecipe lastRelevantRecipe = null;
	private int mintTime = 0;
	public int getMintTime() { return this.mintTime; }
	public float getMintProgress() { return (float)this.mintTime/(float)this.getExpectedMintTime(); }
	public int getExpectedMintTime() { if(this.lastRelevantRecipe != null) return this.lastRelevantRecipe.getDuration(); return -1; }

	private List<CoinMintRecipe> getCoinMintRecipes()
	{
		if(this.level != null)
			return getCoinMintRecipes(this.level);
		return Lists.newArrayList();
	}
    
	public static List<CoinMintRecipe> getCoinMintRecipes(Level level) { return RecipeValidator.getValidMintRecipes(level); }
	
	public CoinMintBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COIN_MINT.get(), pos, state); }
	
	protected CoinMintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.storage.withListener(this::onInventoryChanged);
	}
	
	@Override
	public void saveAdditional(CompoundTag compound,DataContext<Tag> context)
	{
        compound.put("Inventory",context.write(this.storage::serializeNBT));
		compound.putInt("MintTime", this.mintTime);
		super.saveAdditional(compound,context);
	}
	
	@Override
	public void loadAdditional(CompoundTag compound,DataContext<Tag> context)
	{
		super.loadAdditional(compound,context);

		if(compound.contains("Storage"))
            this.storage.safeLoad(compound,"Storage",context);
        if(compound.contains("Inventory"))
            this.storage.deserializeNBT(context.registryAccess(),compound.getCompound("Inventory"));

		if(compound.contains("MintTime"))
			this.mintTime = compound.getInt("MintTime");

	}

	@Override
	public void onLoad() {
		this.lastRelevantRecipe = this.getRelevantRecipe();
	}

	private void onInventoryChanged()
	{
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
		if(this.lastRelevantRecipe != null && this.storage.getStackInSlot(0).getCount() >= this.lastRelevantRecipe.ingredientCount && this.hasOutputSpace())
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
        this.sendPacket(this.builder().setInt("mint_time",this.mintTime));
	}

	public void dumpContents(Level world, BlockPos pos) { ItemHandlerUtil.dropContents(world, pos, this.storage); }
	
	//Coin Minting Functions
	public boolean validMintInput(ItemStack item)
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
		ItemStack currentOutputSlot = this.getStorage().getStackInSlot(1);
		if(currentOutputSlot.isEmpty())
			return true;
		else if(!ItemStack.isSameItemSameComponents(currentOutputSlot, mintOutput))
			return false;
		return currentOutputSlot.getMaxStackSize() - currentOutputSlot.getCount() >= this.lastRelevantRecipe.getOutputItem().getCount();
	}

	@Nullable
	public CoinMintRecipe getRelevantRecipe()
	{
		ItemStack mintInput = this.getStorage().getStackInSlot(0);
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
		if(this.storage.getStackInSlot(0).getCount() < this.lastRelevantRecipe.ingredientCount)
			return;
		
		//Place the output item(s)
		if(this.getStorage().getStackInSlot(1).isEmpty())
		{
			this.getStorage().setStackInSlot(1,mintOutput);
		}
		else
		{
			this.getStorage().getStackInSlot(1).grow(mintOutput.getCount());
		}
		
		//Remove the input item(s)
		this.getStorage().extractItem(0,this.lastRelevantRecipe.ingredientCount,false);
		
		//Job is done!
		this.setChanged();
		
	}

    @Override
    public void handleMessage(Player player, LazyPacketData message) {
        super.handleMessage(player, message);
        if(this.isClient() && message.contains("mint_time"))
            this.mintTime = message.getInt("mint_time");
    }

    public class CoinMintInventory extends LCItemStackHandler
    {
        private CoinMintInventory() { super(2); }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && CoinMintBlockEntity.this.validMintInput(stack); }
    }
	
}
