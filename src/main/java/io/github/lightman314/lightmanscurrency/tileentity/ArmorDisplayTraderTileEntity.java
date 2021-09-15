package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;

public class ArmorDisplayTraderTileEntity extends ItemTraderTileEntity{

	public static final int TRADE_COUNT = 4;
	
	ArmorStandEntity armorStand;
	UUID armorStandID = null;
	
	public ArmorDisplayTraderTileEntity()
	{
		super(ModTileEntities.ARMOR_TRADER, TRADE_COUNT);
		this.validateTradeLimitations();
	}
	
	private void spawnArmorStand()
	{
		if(world == null)
			return;
		
		if(this.world.isRemote)
			return;
		
		this.armorStand = new ArmorStandEntity(world, this.getPos().getX() + 0.5d, this.getPos().getY(), this.getPos().getZ() + 0.5d);
		this.armorStand.setLocationAndAngles(this.getPos().getX() + 0.5d, this.getPos().getY(), this.getPos().getZ() + 0.5d, this.getStandRotation(), 0.0F);
		
		this.armorStand.setInvulnerable(true);
		//this.armorStand.setInvisible(true);
		this.armorStand.setNoGravity(true);
		this.armorStand.setSilent(true);
		CompoundNBT compound = this.armorStand.writeWithoutTypeId(new CompoundNBT());
		compound.putBoolean("Marker", true);
		compound.putBoolean("NoBasePlate", true);
		this.armorStand.read(compound);
		
		this.world.addEntity(this.armorStand);
		
	}
	
	private void validateTradeLimitations()
	{
		if(this.tradeCount > 0)
			this.restrictTrade(0, ItemTradeData.ItemTradeRestrictions.ARMOR_HEAD);
		if(this.tradeCount > 1)
			this.restrictTrade(1, ItemTradeData.ItemTradeRestrictions.ARMOR_CHEST);
		if(this.tradeCount > 2)
			this.restrictTrade(2, ItemTradeData.ItemTradeRestrictions.ARMOR_LEGS);
		if(this.tradeCount > 3)
			this.restrictTrade(3, ItemTradeData.ItemTradeRestrictions.ARMOR_FEET);
	}
	
	@Override
	public void tick()
	{
		
		super.tick();
		
		this.validateTradeLimitations();
		
		if(!world.isRemote) //Only update armor stand values server-side. The entity will update the client manually.
		{
			
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
				//CurrencyMod.LOGGER.info("Updating armor stand info.");
				validateArmorStandValues();
				this.armorStand.setLocationAndAngles(this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5f, this.getStandRotation(), 0f);
				for(int i = 0; i < 4 && i < this.tradeCount; i++)
				{
					ItemTradeData thisTrade = this.getTrade(i);
					//Trade restrictions shall determine the slot type
					EquipmentSlotType slot = ItemTradeData.getSlotFromRestriction(thisTrade.getRestriction());
					if(slot != null)
					{
						if(thisTrade.hasStock(this) || this.isCreative())
							this.armorStand.setItemStackToSlot(slot, thisTrade.getSellItem());
						else
							this.armorStand.setItemStackToSlot(slot, ItemStack.EMPTY);
					}
				}
			}
			
		}
		
	}
	
	protected void validateArmorStand()
	{
		if(this.armorStandID == null)
			return;
		if(this.armorStand != null)
		{
			if(!this.armorStand.getUniqueID().equals(this.armorStandID))
			{
				ArmorStandEntity newArmorStand = getArmorStand(this.armorStandID);
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
		if(!this.armorStand.hasNoGravity())
			this.armorStand.setNoGravity(true);
		if(!this.armorStand.isSilent())
			this.armorStand.setSilent(true);
		if(!this.armorStand.hasMarker() || !this.armorStand.hasNoBasePlate())
		{
			CompoundNBT compound = this.armorStand.writeWithoutTypeId(new CompoundNBT());
			if(!this.armorStand.hasMarker())
				compound.putBoolean("Marker", true);
			if(!this.armorStand.hasNoBasePlate())
				compound.putBoolean("NoBasePlate", true);
			this.armorStand.read(compound);
		}
	}
	
	public void destroyArmorStand()
	{
		if(this.armorStand != null)
			this.armorStand.remove();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		writeArmorStandData(compound);
		return super.write(compound);
	}
	
	protected CompoundNBT writeArmorStandData(CompoundNBT compound)
	{
		if(this.armorStand != null)
			compound.putUniqueId("ArmorStand", this.armorStand.getUniqueID());
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		super.read(state, compound);
		if(compound.contains("ArmorStand"))
		{
			this.armorStandID = compound.getUniqueId("ArmorStand");
		}
		
	}
	
	
	protected ArmorStandEntity getArmorStand(UUID id)
	{
		Entity entity = null;
		if(world instanceof ServerWorld)
		{
			entity = ((ServerWorld)world).getEntityByUuid(id);
		}
		
		if(entity != null && entity instanceof ArmorStandEntity)
			return (ArmorStandEntity)entity;
		
		LightmansCurrency.LogError("Could not find an armor stand with UUID " + id);
		return null;
	}
	
	protected float getStandRotation()
	{
		Direction facing = Direction.NORTH;
		if(this.getBlockState().hasProperty(RotatableBlock.FACING))
			facing = this.getBlockState().get(RotatableBlock.FACING);
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
