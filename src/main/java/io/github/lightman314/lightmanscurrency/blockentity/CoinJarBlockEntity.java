package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CoinJarBlockEntity extends TickableBlockEntity
{
	
	public static int COIN_LIMIT = 64;
	
	List<ItemStack> storage = new ArrayList<>();
	public List<ItemStack> getStorage() { return storage; }
	
	private boolean firstTick = true;
	
	public CoinJarBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.COIN_JAR, pos, state);
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
			if(InventoryUtil.ItemMatches(coin, storage.get(i)))
			{
				if(storage.get(i).getCount() < storage.get(i).getMaxStackSize())
				{
					storage.get(i).grow(1);
					foundStack = true;
				}
			}
		}
		if(!foundStack)
		{
			ItemStack newCoin = coin.copy();
			newCoin.setCount(1);
			storage.add(newCoin);
		}
		
		if(!level.isClientSide)
		{
			CompoundTag compound = this.writeStorage(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, super.save(compound));
		}
		return true;
	}
	
	protected int getCurrentCount()
	{
		int count = 0;
		for(int i = 0; i < storage.size(); i++)
			count += storage.get(i).getCount();
		return count;
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
	{
		this.writeStorage(compound);
		
		return super.save(compound);
	}
	
	protected CompoundTag writeStorage(CompoundTag compound)
	{
		ListTag storageList = new ListTag();
		for(int i = 0; i < storage.size(); i++)
			storageList.add(storage.get(i).save(new CompoundTag()));
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
		
		super.load(compound);
		
	}

	@Override
	public void clientTick()
	{
		if(firstTick)
		{
			firstTick = false;
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(this));
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.getBlockPos(), 0, this.save(new CompoundTag()));
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		CompoundTag compound = pkt.getTag();
		this.load(compound);
	}
	
	//For reading/writing the storage when silk touched.
	public void writeItemTag(ItemStack item)
	{
		CompoundTag compound = item.getOrCreateTag();
		compound.put("JarData", this.writeStorage(new CompoundTag()));
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
					storage = new ArrayList<>();
					ListTag storageList = jarData.getList("Coins", Tag.TAG_COMPOUND);
					for(int i = 0; i < storageList.size(); i++)
					{
						CompoundTag thisItem = storageList.getCompound(i);
						storage.add(ItemStack.of(thisItem));
					}
				}
			}
		}
	}
	
}
