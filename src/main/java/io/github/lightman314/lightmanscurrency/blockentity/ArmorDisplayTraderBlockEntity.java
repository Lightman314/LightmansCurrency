package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.armor_display.*;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;

public class ArmorDisplayTraderBlockEntity extends ItemTraderBlockEntity{

	public static final int TRADE_COUNT = 4;
	
	ArmorStand armorStand;
	UUID armorStandID = null;
	private int armorStandEntityId = -1;
	int requestTimer = 0;
	
	public ArmorDisplayTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.ARMOR_TRADER, pos, state, TRADE_COUNT);
	}
	
	private void spawnArmorStand()
	{
		if(this.level == null)
			return;
		
		if(this.level.isClientSide)
			return;
		
		this.armorStand = new ArmorStand(level, this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5d);
		this.armorStand.moveTo(this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5d, this.getStandRotation(), 0.0F);
		
		this.armorStand.setInvulnerable(true);
		this.armorStand.setNoGravity(true);
		this.armorStand.setSilent(true);
		CompoundTag compound = this.armorStand.saveWithoutId(new CompoundTag());
		compound.putBoolean("Marker", true);
		compound.putBoolean("NoBasePlate", true);
		this.armorStand.load(compound);
		
		this.level.addFreshEntity(this.armorStand);
		
	}
	
	@Override
	public ItemTradeRestriction getRestriction(int tradeIndex) {
		switch(tradeIndex % 4)
		{
		case 0:
			return new EquipmentRestriction(EquipmentSlot.HEAD, this::getArmorStand);
		case 1:
			return new EquipmentRestriction(EquipmentSlot.CHEST, this::getArmorStand);
		case 2:
			return new EquipmentRestriction(EquipmentSlot.LEGS, this::getArmorStand);
		case 3:
			return new EquipmentRestriction(EquipmentSlot.FEET, this::getArmorStand);
			default:
				return ItemTradeRestriction.NONE;
		}
	}
	
	@Override
	public void clientTick() {

		super.clientTick();

		if(this.getArmorStand() == null)
		{
			if(this.requestTimer <= 0)
			{
				this.requestTimer = 20;
				LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestArmorStandID(this.worldPosition));
			}
			else
				this.requestTimer--;
		}
	}
	
	@Override
	public void serverTick()
	{
		
		super.serverTick();
		
		if(this.armorStandID != null)
		{
			validateArmorStand();
			this.armorStandID = null;
		}
		//Validate armor stand values
		if(this.armorStand == null || !this.armorStand.isAlive())
		{
			//Armor stand has been deleted. Spawn a new one.
			spawnArmorStand();
		}
		if(this.armorStand != null)
		{
			validateArmorStandValues();
			this.armorStand.moveTo(this.worldPosition.getX() + 0.5d, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5f, this.getStandRotation(), 0f);
			for(int i = 0; i < 4 && i < this.tradeCount; i++)
			{
				ItemTradeData thisTrade = this.getTrade(i);
				//Trade restrictions shall determine the slot type
				ItemTradeRestriction r = thisTrade.getRestriction();
				EquipmentSlot slot = null;
				if(r instanceof EquipmentRestriction)
				{
					EquipmentRestriction er = (EquipmentRestriction)r;
					slot = er.getEquipmentSlot();
				}
				if(slot != null)
				{
					if(thisTrade.hasStock(this) || this.getCoreSettings().isCreative())
						this.armorStand.setItemSlot(slot, thisTrade.getSellItem(0));
					else
						this.armorStand.setItemSlot(slot, ItemStack.EMPTY);
				}
			}
		}
		
	}
	
	public void sendArmorStandSyncMessageToClient(PacketTarget target) {
		this.validateArmorStand();
		if(this.armorStand != null)
		{
			LightmansCurrencyPacketHandler.instance.send(target, new MessageSendArmorStandID(this.worldPosition, this.armorStand.getId()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void receiveArmorStandID(BlockPos pos, int entityId) {
		Minecraft mc = Minecraft.getInstance();
		BlockEntity be = mc.level.getBlockEntity(pos);
		if(be instanceof ArmorDisplayTraderBlockEntity)
		{
			//LightmansCurrency.LogInfo("Received Armor Stand id " + entityId + " from the server.");
			((ArmorDisplayTraderBlockEntity)be).armorStandEntityId = entityId;
		}
	}
	
	protected void validateArmorStand()
	{
		if(this.armorStandID == null)
			return;
		if(this.armorStand != null)
		{
			if(!this.armorStand.getUUID().equals(this.armorStandID))
			{
				ArmorStand newArmorStand = getArmorStand(this.armorStandID);
				if(newArmorStand != null)
				{
					destroyArmorStand();
					this.armorStand = newArmorStand;
				}
			}
		}
		else
		{
			this.armorStand = getArmorStand(this.armorStandID);
		}
	}
	
	protected void validateArmorStandValues()
	{
		if(!this.armorStand.isInvulnerable())
			this.armorStand.setInvulnerable(true);
		if(this.armorStand.isInvisible())
			this.armorStand.setInvisible(false);
		if(!this.armorStand.noPhysics)
			this.armorStand.setNoGravity(true);
		if(!this.armorStand.isSilent())
			this.armorStand.setSilent(true);
		if(!this.armorStand.isMarker() || !this.armorStand.isNoBasePlate())
		{
			CompoundTag compound = this.armorStand.saveWithoutId(new CompoundTag());
			if(!this.armorStand.isMarker())
				compound.putBoolean("Marker", true);
			if(!this.armorStand.isNoBasePlate())
				compound.putBoolean("NoBasePlate", true);
			this.armorStand.load(compound);
		}
	}
	
	public void destroyArmorStand()
	{
		if(this.armorStand != null)
			this.armorStand.remove(RemovalReason.DISCARDED);
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		writeArmorStandData(compound);
		
		super.saveAdditional(compound);
	}
	
	protected CompoundTag writeArmorStandData(CompoundTag compound)
	{
		if(this.armorStand != null)
			compound.putUUID("ArmorStand", this.armorStand.getUUID());
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		if(compound.contains("ArmorStand"))
		{
			this.armorStandID = compound.getUUID("ArmorStand");
		}
		super.load(compound);
	}
	
	protected ArmorStand getArmorStand() {
		if(this.isServer())
			return this.getArmorStand(this.armorStandID);
		else
		{
			Entity entity = this.level.getEntity(this.armorStandEntityId);
			if(entity != null && entity instanceof ArmorStand)
				return (ArmorStand)entity;
			return null;
		}
	}
	
	protected ArmorStand getArmorStand(UUID id)
	{
		Entity entity = null;
		if(this.level instanceof ServerLevel)
		{
			entity = ((ServerLevel)level).getEntity(id);
		}
		
		if(entity != null && entity instanceof ArmorStand)
			return (ArmorStand)entity;
		
		LightmansCurrency.LogError("Could not find an armor stand with UUID " + id);
		return null;
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
	
}
