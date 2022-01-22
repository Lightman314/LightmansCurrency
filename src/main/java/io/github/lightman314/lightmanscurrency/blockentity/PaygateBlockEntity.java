package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.menus.PaygateMenu;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PaygateBlockEntity extends TickableBlockEntity implements MenuProvider, Nameable {
	
	public static final int PRICE_MIN = 0;
	public static final int PRICE_MAX = Integer.MAX_VALUE;
	
	public static final int DURATION_MIN = 1;
	public static final int DURATION_MAX = 200;
	
	private Component customName;
	private UUID ownerID = null;
	private UUID ticketID = null;
	private String ownerName = "";
	private CoinValue storedMoney = new CoinValue();
	private CoinValue price = new CoinValue();
	private int duration = 40;
	private int timer = 0;
	
	public PaygateBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.PAYGATE, pos, state);
	}
	
	public void setOwner(Entity player)
	{
		this.ownerID = player.getUUID();
		this.ownerName = player.getName().getString();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeOwner(new CompoundTag()));
		}
	}
	
	public boolean isOwner(Entity player)
	{
		if(this.ownerID == null)
			return true;
		return player.getUUID().equals(this.ownerID);
	}
	
	public boolean canBreak(Player player)
	{
		if(this.isOwner(player))
			return true;
		return TradingOffice.isAdminPlayer(player);
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
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeStoredMoney(new CompoundTag()));
		}
	}
	
	public void clearStoredMoney()
	{
		this.storedMoney = new CoinValue();
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeStoredMoney(new CompoundTag()));
		}
	}
	
	public CoinValue getPrice()
	{
		return this.price;
	}
	
	public void setPrice(CoinValue value)
	{
		this.price = value;
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writePrice(new CompoundTag()));
		}
	}
	
	public int getDuration()
	{
		return this.duration;
	}
	
	public void setDuration(int value)
	{
		this.duration = MathUtil.clamp(value, 1, 200);
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeDuration(new CompoundTag()));
		}
	}
	
	public void SetTicketID(UUID ticketID)
	{
		this.ticketID = ticketID;
		if(!this.level.isClientSide)
		{
			BlockEntityUtil.sendUpdatePacket(this, this.writeTicket(new CompoundTag()));
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
		this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, true));
		//this.getBlockState().updateNeighbours(this.world, this.pos, 35);
	}
	
	@Override
	public void onLoad()
	{
		if(this.level.isClientSide)
		{
			//CurrencyMod.LOGGER.info("Loaded client-side PaygateTileEntity. Requesting update packet.");
			BlockEntityUtil.requestUpdatePacket(this);
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		
		writeOwner(compound);
		writeStoredMoney(compound);
		writePrice(compound);
		writeDuration(compound);
		writeTimer(compound);
		writeTicket(compound);
		
		if(this.customName != null)
			compound.putString("CustomName", Component.Serializer.toJson(this.customName));
		
		super.saveAdditional(compound);
		
	}
	
	private CompoundTag writeOwner(CompoundTag compound)
	{
		if(this.ownerID != null)
			compound.putUUID("OwnerID", this.ownerID);
		compound.putString("OwnerName", this.ownerName);
		return compound;
	}
	
	private CompoundTag writeStoredMoney(CompoundTag compound)
	{
		//compound.putInt("StoredMoney", this.storedMoney);
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		return compound;
	}
	
	private CompoundTag writePrice(CompoundTag compound)
	{
		//compound.putInt("Price", this.price);
		this.price.writeToNBT(compound, "Price");
		return compound;
	}
	
	private CompoundTag writeDuration(CompoundTag compound)
	{
		compound.putInt("Duration", this.duration);
		return compound;
	}
	
	private CompoundTag writeTimer(CompoundTag compound)
	{
		compound.putInt("Timer", this.timer);
		return compound;
	}
	
	private CompoundTag writeTicket(CompoundTag compound)
	{
		if(this.ticketID != null)
			compound.putUUID("TicketID", this.ticketID);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		
		//Read owner
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUUID("OwnerID");
		if(compound.contains("OwnerName", Tag.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Read stored money & current price
		this.storedMoney.readFromNBT(compound, "StoredMoney");
		
		this.price.readFromNBT(compound, "Price");
		
		//Read the output customization
		if(compound.contains("Duration", Tag.TAG_INT))
			this.duration = compound.getInt("Duration");
		//Read the timer
		if(compound.contains("Timer", Tag.TAG_INT))
			this.timer = compound.getInt("Timer");
		//Read the ticket ID
		if(compound.contains("TicketID"))
			this.ticketID = compound.getUUID("TicketID");
		//Read the custom name
		if (compound.contains("CustomName", Tag.TAG_STRING))
			this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
		
		super.load(compound);
		
	}
	
	@Override
	public void tick()
	{
		if(this.timer > 0)
		{
			this.timer--;
			if(!this.level.isClientSide)
			{
				BlockEntityUtil.sendUpdatePacket(this, this.writeTimer(new CompoundTag()));
			}
			if(timer <= 0)
			{
				this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, false));
			}
		}
	}
	
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		return new PaygateMenu(windowId, inventory, this);
	}

	@Override
	public Component getDisplayName() {
		return getTitle();
	}
	
	public Component getTitle()
	{
		return new TranslatableComponent("gui.lightmanscurrency.paygate.title", getName(), this.ownerName);
	}

	@Override
	public Component getName() {
		if(this.customName == null)
			return new TranslatableComponent("block.lightmanscurrency.paygate");
		return this.customName;
	}
	
	public void setCustomName(Component customName)
	{
		this.customName = customName;
	}
	
	@Override
	public CompoundTag getUpdateTag() { return this.saveWithFullMetadata(); }
	
}
