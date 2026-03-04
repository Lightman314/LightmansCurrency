package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.capability.implementations.MoneyViewWrapper;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.items.CoinJarItem;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class CoinJarBlockEntity extends EasyBlockEntity
{
	
	public static final int COIN_LIMIT = 64;

	private int color = -1;
	public int getColor() { return this.color >= 0 ? this.color : 0xFFFFFF; }

	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return this.storage; }
	public void clearStorage() { this.storage.clear(); }
	
	private final ItemViewer viewer = new ItemViewer(this);
    private final IMoneyViewer moneyViewer = MoneyViewWrapper.forInventory(this.viewer,this);
	public IItemHandler getViewer() { return this.viewer; }
    public IMoneyViewer getMoneyViewer() { return this.moneyViewer; }
	
	public CoinJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.COIN_JAR.get(), pos, state);
	}
	
	public boolean addCoin(ItemStack coin)
	{
		if(getCurrentCount() >= COIN_LIMIT)
			return false;
		if(!CoinAPI.getApi().IsAllowedInCoinContainer(coin, false))
			return false;
		
		boolean foundStack = false;
		for(int i = 0; i < storage.size() && !foundStack; i++)
		{
			if(ItemStack.isSameItemSameComponents(coin,this.storage.get(i)))
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
			ItemStack newCoin = coin.copyWithCount(1);
			this.storage.add(newCoin);
		}
		
		if(!this.level.isClientSide)
			BlockEntityUtil.sendUpdatePacket(this,this.writeStorage(new CompoundTag(),DataContext.createNBT(this.registryAccess())));
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
	public void saveAdditional(CompoundTag compound,DataContext<Tag> context)
	{
		this.writeStorage(compound,context);

		if(this.color >= 0)
			compound.putInt("Color", this.color);
		
		super.saveAdditional(compound,context);
	}
	
	protected CompoundTag writeStorage(CompoundTag compound,DataContext<Tag> context)
	{
		compound.put("Coins",context.write(this.storage,CodecHelper.UNLIMITED_ITEM_LIST));
		return compound;
	}

	@Override
	protected void loadAdditional(CompoundTag compound,DataContext<Tag> context) {

		if(compound.contains("Coins"))
		{
			this.storage = new ArrayList<>();
            this.storage.addAll(context.readOrDefault(compound.get("Coins"),CodecHelper.UNLIMITED_ITEM_LIST,new ArrayList<>()));
		}

		if(compound.contains("Color"))
			this.color = compound.getInt("Color");

		super.loadAdditional(compound,context);
	}
	
	//For reading/writing the storage when silk touched.
	public void addFullData(ItemStack item)
	{
		if(!this.storage.isEmpty())
			CoinJarItem.setJarContents(item, this.storage);
		this.addSimpleData(item);
	}

	//For writing the color when picked
	public void addSimpleData(ItemStack item)
	{
		if(this.color >= 0)
			CoinJarItem.setJarColor(item,this.color);
	}
	
	public void readItemData(ItemStack item)
	{
		this.storage = ItemHandlerUtil.copyList(CoinJarItem.getJarContents(item));
		if(item.getItem() instanceof CoinJarItem jar && jar.canDye(item))
			this.color = CoinJarItem.getJarColor(item);
	}

	private record ItemViewer(CoinJarBlockEntity be) implements IItemHandler {
		@Override
		public int getSlots() { return this.be.storage.size(); }
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot >= 0 && slot < this.be.storage.size())
				return this.be.storage.get(slot).copy();
			return ItemStack.EMPTY;
		}
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack.copy(); }
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
		@Override
		public int getSlotLimit(int slot) { return 64; }
		@Override
		public boolean isItemValid(int slot, ItemStack stack) { return false; }
	}
	
}
