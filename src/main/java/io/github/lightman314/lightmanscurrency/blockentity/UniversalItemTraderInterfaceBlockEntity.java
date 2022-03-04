package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.blockentity.handler.ItemInterfaceHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.trader.interfacing.ItemTradeInteraction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalItemTraderInterfaceBlockEntity extends UniversalTraderInterfaceBlockEntity<ItemTradeInteraction,ItemTradeData>{

	public static final int BUFFER_SIZE = 9;
	
	private Container itemBuffer = new SimpleContainer(BUFFER_SIZE);
	public Container getItemBuffer() { return this.itemBuffer; }
	
	private final List<ItemTradeInteraction> interactions;
	public List<ItemTradeInteraction> getInteractions() { return this.interactions; }
	
	public UniversalItemTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ARMOR_TRADER, pos, state, ItemTradeData::loadData);
		this.addHandler(new ItemInterfaceHandler(this, this::getItemBuffer));
		this.interactions = new ArrayList<>(INTERACTION_LIMIT);
		while(this.interactions.size() < INTERACTION_LIMIT)
			this.interactions.add(new ItemTradeInteraction(this));
	}

	public void markItemBufferDirty() {
		this.setChanged();
		if(!this.isClient())
		{
			BlockEntityUtil.sendUpdatePacket(this, this.saveItemBuffer(new CompoundTag()));
		}
	}
	
	@Override
	public void markTradeInteractionsDirty() {
		this.setChanged();
		if(!this.isClient())
		{
			BlockEntityUtil.sendUpdatePacket(this, this.saveInteractions(new CompoundTag()));
		}
	}
	
	public int getTotalClaimedSlots() {
		int claimedCount = 0;
		for(ItemTradeInteraction i : this.interactions)
			claimedCount += i.getClaimedSlots();
		return claimedCount;
	}

	@Override
	public RemoteTradeData getRemoteTradeData() {
		return new RemoteTradeData(this.owner, this.getBankAccount(), null, this.itemBuffer, null, null);
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		this.saveItemBuffer(compound);
		this.saveInteractions(compound);
	}
	
	protected final CompoundTag saveItemBuffer(CompoundTag compound) {
		InventoryUtil.saveAllItems("ItemBuffer", compound, this.itemBuffer);
		return compound;
	}
	
	protected final CompoundTag saveInteractions(CompoundTag compound) {
		ListTag interactionList = new ListTag();
		for(int i = 0; i < this.interactions.size(); ++i)
		{
			CompoundTag tag = this.interactions.get(i).save();
			interactionList.add(tag);
		}
		compound.put("Interactions", interactionList);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		if(compound.contains("ItemBuffer", Tag.TAG_LIST))
			this.itemBuffer = InventoryUtil.loadAllItems("ItemBuffer", compound, BUFFER_SIZE);
		if(compound.contains("Interactions", Tag.TAG_LIST))
		{
			ListTag interactionList = compound.getList("Interactions", Tag.TAG_COMPOUND);
			for(int i = 0; i < interactionList.size() && i < this.interactions.size(); ++i)
				this.interactions.get(i).load(interactionList.getCompound(i));
		}
	}
}
