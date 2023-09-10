package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.items.CoinJarItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class CoinJarBlockEntity extends EasyBlockEntity
{

	public static final int COIN_LIMIT = 64;

	private int color = -1;
	public int getColor() { return this.color >= 0 ? this.color : 0xFFFFFF; }

	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return this.storage; }
	public void clearStorage() { this.storage.clear(); }

	private final ItemViewer viewer = new ItemViewer(this);

	public CoinJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.COIN_JAR.get(), pos, state);
	}

	public boolean addCoin(ItemStack coin)
	{
		if(getCurrentCount() >= COIN_LIMIT)
			return false;
		if(!MoneyUtil.isCoin(coin, false))
			return false;

		boolean foundStack = false;
		for(int i = 0; i < storage.size() && !foundStack; i++)
		{
			if(InventoryUtil.ItemMatches(coin, this.storage.get(i)))
			{
				if(this.storage.get(i).getCount() < this.storage.get(i).getMaxStackSize())
				{
					this.storage.get(i).grow(1);
					foundStack = true;
				}
			}
		}
		if(!foundStack)
		{
			ItemStack newCoin = coin.copy();
			newCoin.setCount(1);
			this.storage.add(newCoin);
		}

		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new CompoundTag()));
		}
		return true;
	}

	protected int getCurrentCount()
	{
		int count = 0;
		for (ItemStack stack : storage)
			count += stack.getCount();
		return count;
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound)
	{
		this.writeStorage(compound);

		if(this.color >= 0)
			compound.putInt("Color", this.color);

		super.saveAdditional(compound);
	}

	protected CompoundTag writeStorage(CompoundTag compound)
	{
		ListTag storageList = new ListTag();
		for (ItemStack stack : this.storage)
			storageList.add(stack.save(new CompoundTag()));
		compound.put("Coins", storageList);

		return compound;
	}

	@Override
	public void load(CompoundTag compound)
	{

		if(compound.contains("Coins"))
		{
			storage = new ArrayList<>();
			ListTag storageList = compound.getList("Coins", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundTag thisItem = storageList.getCompound(i);
				storage.add(ItemStack.of(thisItem));
			}
		}

		if(compound.contains("Color"))
			this.color = compound.getInt("Color");

		super.load(compound);

	}

	@Override
	public void onLoad()
	{
		if(this.level.isClientSide)
		{
			BlockEntityUtil.requestUpdatePacket(this);
		}
	}

	//For reading/writing the storage when silk touched.
	public void writeItemTag(ItemStack item)
	{
		if(this.storage.size() > 0)
			item.getOrCreateTag().put("JarData", this.writeStorage(new CompoundTag()));
		this.writeSimpleItemTag(item);
	}

	//For writing the color when picked
	public void writeSimpleItemTag(ItemStack item)
	{
		if(this.color >= 0)
		{
			CompoundTag compound = item.getOrCreateTag();
			CompoundTag displayTag = new CompoundTag();
			displayTag.putInt(DyeableLeatherItem.TAG_COLOR, this.color);
			compound.put(DyeableLeatherItem.TAG_DISPLAY, displayTag);
		}
	}

	public void readItemTag(ItemStack item)
	{
		if(item.hasTag())
		{
			CompoundTag compound = item.getTag();
			if(compound.contains("JarData", Tag.TAG_COMPOUND))
			{
				CompoundTag jarData = compound.getCompound("JarData");
				if(jarData.contains("Coins"))
				{
					this.storage = new ArrayList<>();
					ListTag storageList = jarData.getList("Coins", Tag.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundTag thisItem = storageList.getCompound(i);
						this.storage.add(ItemStack.of(thisItem));
					}
				}
			}
			if(item.getItem() instanceof CoinJarItem.Colored coloredJar)
				this.color = coloredJar.getColor(item);
			this.setChanged();
		}
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this.viewer));
		return super.getCapability(cap, side);
	}

	private record ItemViewer(CoinJarBlockEntity be) implements IItemHandler {

		@Override
		public int getSlots() { return this.be.storage.size(); }

		@Override
		@Nonnull
		public ItemStack getStackInSlot(int slot) {
			if (slot >= 0 && slot < this.be.storage.size())
				return this.be.storage.get(slot).copy();
			return ItemStack.EMPTY;
		}

		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			return stack.copy();
		}

		@Override
		@Nonnull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) { return 64; }

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return false; }
	}

}