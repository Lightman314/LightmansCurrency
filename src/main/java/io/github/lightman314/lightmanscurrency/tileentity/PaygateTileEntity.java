package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.containers.PaygateContainer;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

public class PaygateTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, INameable {
	
	public static final int PRICE_MIN = 0;
	public static final int PRICE_MAX = Integer.MAX_VALUE;
	
	public static final int DURATION_MIN = 1;
	public static final int DURATION_MAX = 200;
	
	private ITextComponent customName;
	private UUID ownerID = null;
	private UUID ticketID = null;
	private String ownerName = "";
	private CoinValue storedMoney = new CoinValue();
	private CoinValue price = new CoinValue();
	private int duration = 40;
	private int timer = 0;
	
	public PaygateTileEntity()
	{
		super(ModTileEntities.PAYGATE);
	}
	
	public void setOwner(Entity player)
	{
		this.ownerID = player.getUniqueID();
		this.ownerName = player.getName().getString();
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeOwner(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public boolean isOwner(Entity player)
	{
		if(this.ownerID == null)
			return true;
		return player.getUniqueID().equals(this.ownerID);
	}
	
	public boolean canBreak(PlayerEntity player)
	{
		if(this.isOwner(player))
			return true;
		return player.hasPermissionLevel(2) && player.isCreative();
	}
	
	public boolean isActive()
	{
		return timer > 0;
	}
	
	public CoinValue getStoredMoney()
	{
		return this.storedMoney;
	}
	
	public void addStoredMoney(CoinValue amount)
	{
		this.storedMoney.addValue(amount);;
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeStoredMoney(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public void clearStoredMoney()
	{
		this.storedMoney = new CoinValue();
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeStoredMoney(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public CoinValue getPrice()
	{
		return this.price;
	}
	
	public void setPrice(CoinValue value)
	{
		this.price = value;
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writePrice(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public int getDuration()
	{
		return this.duration;
	}
	
	public void setDuration(int value)
	{
		this.duration = MathUtil.clamp(value, 1, 200);
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeDuration(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public void SetTicketID(UUID ticketID)
	{
		this.ticketID = ticketID;
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeTicket(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
	}
	
	public boolean HasPairedTicket()
	{
		return this.ticketID != null;
	}
	
	public boolean validTicket(ItemStack ticket)
	{
		if(ticket.isEmpty())
			return false;
		if(ticket.getItem() instanceof TicketItem)
			return !TicketItem.isMasterTicket(ticket) && validTicket(TicketItem.GetTicketID(ticket));
		else
			return false;
	}
	
	public boolean validTicket(UUID ticketID)
	{
		if(this.ticketID == null || ticketID == null)
			return false;
		else
		{
			if(this.ticketID.equals(ticketID))
				return true;
		}
		return false;
	}
	
	public void activate()
	{
		this.timer = this.duration;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(PaygateBlock.POWERED, true));
		//this.getBlockState().updateNeighbours(this.world, this.pos, 35);
	}
	
	@Override
	public void onLoad()
	{
		if(this.world.isRemote)
		{
			//CurrencyMod.LOGGER.info("Loaded client-side PaygateTileEntity. Requesting update packet.");
			TileEntityUtil.requestUpdatePacket(this.world, this.pos);
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		writeOwner(compound);
		writeStoredMoney(compound);
		writePrice(compound);
		writeDuration(compound);
		writeTimer(compound);
		writeTicket(compound);
		
		if(this.customName != null)
			compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
		
		return super.write(compound);
		
	}
	
	private CompoundNBT writeOwner(CompoundNBT compound)
	{
		if(this.ownerID != null)
			compound.putUniqueId("OwnerID", this.ownerID);
		compound.putString("OwnerName", this.ownerName);
		return compound;
	}
	
	private CompoundNBT writeStoredMoney(CompoundNBT compound)
	{
		//compound.putInt("StoredMoney", this.storedMoney);
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		return compound;
	}
	
	private CompoundNBT writePrice(CompoundNBT compound)
	{
		//compound.putInt("Price", this.price);
		this.price.writeToNBT(compound, "Price");
		return compound;
	}
	
	private CompoundNBT writeDuration(CompoundNBT compound)
	{
		compound.putInt("Duration", this.duration);
		return compound;
	}
	
	private CompoundNBT writeTimer(CompoundNBT compound)
	{
		compound.putInt("Timer", this.timer);
		return compound;
	}
	
	private CompoundNBT writeTicket(CompoundNBT compound)
	{
		if(this.ticketID != null)
			compound.putUniqueId("TicketID", this.ticketID);
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		//Read owner
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUniqueId("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Read stored money & current price
		if(compound.contains("StoredMoney", Constants.NBT.TAG_INT))
		{
			this.storedMoney.readFromOldValue(compound.getInt("StoredMoney"));
			LightmansCurrency.LogInfo("Reading stored money from older value format. Will be updated to newer value format.");
		}
		else if(compound.contains("StoredMoney"))
			this.storedMoney.readFromNBT(compound, "StoredMoney");
		//Read the output customization
		if(compound.contains("Price", Constants.NBT.TAG_INT))
		{
			this.price.readFromOldValue(compound.getInt("Price"));
			LightmansCurrency.LogInfo("Reading price from older value format. Will be updated to newer value format.");
		}
		else if(compound.contains("Price"))
			this.price.readFromNBT(compound, "Price");
		if(compound.contains("Duration", Constants.NBT.TAG_INT))
			this.duration = compound.getInt("Duration");
		//Read the timer
		if(compound.contains("Timer", Constants.NBT.TAG_INT))
			this.timer = compound.getInt("Timer");
		//Read the ticket ID
		if(compound.contains("TicketID"))
			this.ticketID = compound.getUniqueId("TicketID");
		//Read the custom name
		if (compound.contains("CustomName", Constants.NBT.TAG_STRING))
			this.customName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
		
		
		
		super.read(state, compound);
		
	}
	
	@Override
	public void tick()
	{
		if(timer > 0)
		{
			timer--;
			if(!this.world.isRemote)
			{
				CompoundNBT compound = this.writeTimer(new CompoundNBT());
				TileEntityUtil.sendUpdatePacket(this, super.write(compound));
			}
			if(timer <= 0)
			{
				this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(PaygateBlock.POWERED, false));
			}
		}
	}
	
	@Override
	public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
		return new PaygateContainer(windowId, inventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return getTitle();
	}
	
	public ITextComponent getTitle()
	{
		return new TranslationTextComponent("gui.lightmanscurrency.paygate.title", getName(), this.ownerName);
	}

	@Override
	public ITextComponent getName() {
		if(this.customName == null)
			return new TranslationTextComponent("block.lightmanscurrency.paygate");
		return this.customName;
	}
	
	public void setCustomName(ITextComponent customName)
	{
		this.customName = customName;
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 0, this.write(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		CompoundNBT compound = pkt.getNbtCompound();
		//CurrencyMod.LOGGER.info("Loading NBT from update packet.");
		this.read(this.getBlockState(), compound);
	}
	
}
