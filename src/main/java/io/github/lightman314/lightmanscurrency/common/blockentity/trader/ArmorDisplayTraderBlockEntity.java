package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.armor_display.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataArmor;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ArmorDisplayTraderBlockEntity extends ItemTraderBlockEntity {
	public static final int TRADE_COUNT = 4;
	private static final int TICK_DELAY = 20;
	
	UUID armorStandID = null;
	private int armorStandEntityId = -1;
	int requestTimer = TICK_DELAY;
	
	int updateTimer = TICK_DELAY;
	
	private boolean loaded = false;
	public void flagAsLoaded() { this.loaded = true; }
	
	public ArmorDisplayTraderBlockEntity()
	{
		super(ModBlockEntities.ARMOR_TRADER.get(), TRADE_COUNT);
	}
	
	@Override
	public ItemTraderData buildNewTrader() { return new ItemTraderDataArmor(this.level, this.worldPosition); }


	@Override
	public void tick() {
		super.tick();
		if(this.isClient())
			this.clientTick();
		else
			this.serverTick();
	}

	private void clientTick() {
		if(this.getArmorStand() == null)
		{
			if(this.requestTimer <= 0)
			{
				this.requestTimer = TICK_DELAY;
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestArmorStandID(this.worldPosition));
			}
			else
				this.requestTimer--;
		}
	}

	private void serverTick()
	{
		if(!this.loaded)
			return;
		
		if(this.updateTimer <= 0)
		{
			this.updateTimer = TICK_DELAY;
			this.validateArmorStand();
			this.validateArmorStandValues();
			this.updateArmorStandArmor();
			this.killIntrudingArmorStands();
		}
		else
			this.updateTimer--;
	}
	
	/**
	 * Validates the armor stands existence, the local ArmorStandID, and gets the local reference to the armor stand.
	 * Logical Server only.
	 */
	public void validateArmorStand() {
		if(this.isClient())
			return;
		ArmorStandEntity armorStand = this.getArmorStand();
		if(armorStand == null || !armorStand.isAlive())
		{
			//Spawn a new armor stand
			this.spawnArmorStand();
		}
	}
	
	private void spawnArmorStand()
	{
		if(this.level == null || this.isClient())
			return;
		
		ArmorStandEntity armorStand = new ArmorStandEntity(this.level, this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5d);
		armorStand.moveTo(this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5d, this.getStandRotation(), 0.0F);
		
		armorStand.setInvulnerable(true);
		armorStand.setNoGravity(true);
		armorStand.setSilent(true);
		CompoundNBT compound = armorStand.saveWithoutId(new CompoundNBT());
		compound.putBoolean("Marker", true);
		compound.putBoolean("NoBasePlate", true);
		armorStand.load(compound);
		
		this.level.addFreshEntity(armorStand);
		
		this.armorStandID = armorStand.getUUID();
		this.setChanged();
		
	}
	
	protected void updateArmorStandArmor() {
		ArmorStandEntity armorStand = this.getArmorStand();
		if(armorStand != null)
		{
			ItemTraderData trader = this.getTraderData();
			if(trader != null)
			{
				List<ItemTradeData> trades = trader.getTradeData();
				for(int i = 0; i < 4 && i < trades.size(); i++)
				{
					ItemTradeData thisTrade = trades.get(i);
					//Trade restrictions shall determine the slot type
					ItemTradeRestriction r = thisTrade.getRestriction();
					EquipmentSlotType slot = null;
					if(r instanceof EquipmentRestriction)
					{
						EquipmentRestriction er = (EquipmentRestriction)r;
						slot = er.getEquipmentSlot();
					}
					if(slot != null)
					{
						if(thisTrade.hasStock(trader) || trader.isCreative())
							armorStand.setItemSlot(slot, thisTrade.getSellItem(0));
						else
							armorStand.setItemSlot(slot, ItemStack.EMPTY);
					}
				}
			}
			
		}
	}
	
	public void killIntrudingArmorStands() {
		ArmorStandEntity armorStand = this.getArmorStand();
		if(this.level != null && armorStand != null)
		{
			this.level.getEntitiesOfClass(ArmorStandEntity.class, this.getBlockState().getShape(this.level, this.worldPosition).bounds()).forEach(as ->{
				//Delete any armor stands in the exact coordinates as our armor stand.
				//Should delete any old duplicates from previously buggy armor stands.
				if(as.position().equals(armorStand.position()))
					as.remove(false);
			});
		}
	}
	
	public void sendArmorStandSyncMessageToClient(PacketDistributor.PacketTarget target) {
		ArmorStandEntity armorStand = this.getArmorStand();
		if(armorStand != null)
		{
			LightmansCurrencyPacketHandler.instance.send(target, new MessageSendArmorStandID(this.worldPosition, armorStand.getId()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void receiveArmorStandID(BlockPos pos, int entityId) {
		Minecraft mc = Minecraft.getInstance();
		TileEntity be = mc.level.getBlockEntity(pos);
		if(be instanceof ArmorDisplayTraderBlockEntity)
		{
			//LightmansCurrency.LogInfo("Received Armor Stand id " + entityId + " from the server.");
			((ArmorDisplayTraderBlockEntity)be).armorStandEntityId = entityId;
		}
	}
	
	protected void validateArmorStandValues()
	{
		ArmorStandEntity armorStand = this.getArmorStand();
		if(armorStand == null)
			return;
		armorStand.moveTo(this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5f, this.getStandRotation(), 0f);
		if(!armorStand.isInvulnerable())
			armorStand.setInvulnerable(true);
		if(armorStand.isInvisible())
			armorStand.setInvisible(false);
		if(!armorStand.noPhysics)
			armorStand.setNoGravity(true);
		if(!armorStand.isSilent())
			armorStand.setSilent(true);
		if(!armorStand.isMarker() || !armorStand.isNoBasePlate())
		{
			CompoundNBT compound = armorStand.saveWithoutId(new CompoundNBT());
			if(!armorStand.isMarker())
				compound.putBoolean("Marker", true);
			if(!armorStand.isNoBasePlate())
				compound.putBoolean("NoBasePlate", true);
			armorStand.load(compound);
		}
	}
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		this.writeArmorStandData(compound);
		
		return compound;
	}
	
	protected void writeArmorStandData(CompoundNBT compound)
	{
		if(this.armorStandID != null)
			compound.putUUID("ArmorStand", this.armorStandID);
	}
	
	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		this.loaded = true;
		if(compound.contains("ArmorStand"))
			this.armorStandID = compound.getUUID("ArmorStand");
	}
	
	protected ArmorStandEntity getArmorStand()
	{
		Entity entity;
		if(this.level instanceof ServerWorld)
			entity = ((ServerWorld)level).getEntity(this.armorStandID);
		else
			entity = this.level.getEntity(this.armorStandEntityId);
		
		if(entity instanceof ArmorStandEntity)
			return (ArmorStandEntity) entity;
		
		return null;
	}
	
	public void destroyArmorStand()
	{
		ArmorStandEntity armorStand = this.getArmorStand();
		if(armorStand != null)
			armorStand.kill();
	}
	
	protected float getStandRotation()
	{
		Direction facing = Direction.NORTH;
		if(this.getBlockState().getBlock() instanceof IRotatableBlock)
			facing = ((IRotatableBlock)this.getBlockState().getBlock()).getFacing(this.getBlockState());
		if(facing == Direction.SOUTH)
			return 180f;
		else if(facing == Direction.NORTH)
			return 0f;
		else if(facing == Direction.WEST)
			return -90f;
		else if(facing == Direction.EAST)
			return 90f;
		return 0f;
	}
	
	@Override @Deprecated
	protected ItemTraderData createTraderFromOldData(CompoundNBT compound) {
		ItemTraderDataArmor newTrader = new ItemTraderDataArmor(this.level, this.worldPosition);
		newTrader.loadOldUniversalTraderData(compound);
		this.tradeCount = newTrader.getTradeCount();
		return newTrader;
	}
	
}