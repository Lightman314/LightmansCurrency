package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.*;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.traders.*;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.AbstractTraderMenu;

import io.github.lightman314.lightmanscurrency.api.traders.tracking.PlayerTraderTrackingHolder;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;

public class TraderMenu extends AbstractTraderMenu {

	private final Supplier<ITraderSource> traderSource;
	@Nullable
	@Override
	public ITraderSource getTraderSource() { return this.traderSource.get(); }
	
	public static final int SLOT_OFFSET = 15;
	
	InteractionSlot interactionSlot;
	public InteractionSlot getInteractionSlot() { return this.interactionSlot; }

	private final MoneyInventory coins;
	
	List<Slot> coinSlots = new ArrayList<>();
	public List<Slot> getCoinSlots() { return this.coinSlots; }

    private final PlayerTraderTrackingHolder trackingHolder = new PlayerTraderTrackingHolder(this,TrackingLevel.CUSTOMER);
    private Set<Long> trackingCache = new HashSet<>();

	public TraderMenu(int windowID, Inventory inventory, long traderID, MenuValidator validator) {
		this(ModMenus.TRADER.get(), windowID, inventory, () -> TraderAPI.getApi().GetTrader(IClientTracker.entityWrapper(inventory.player), traderID), validator);
	}
	
	protected TraderMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<ITraderSource> traderSource, MenuValidator validator) {
		super(type,windowID,inventory,validator);
		this.traderSource = traderSource;
		this.coins = new MoneyInventory(this.player,5);

		this.addValidator(this::traderSourceValid);

		this.init(inventory);
		for(TraderData trader : this.traderSource.get().getTraders()) {
			trader.userOpen(this.player);
            this.trackingHolder.requestTracking(trader,this.player);
            this.trackingCache.add(trader.getID());
		}

        NeoForge.EVENT_BUS.register(this);
	}

	public TradeContext getContext(@Nullable TraderData trader) {
		long traderID = trader == null ? -1 : trader.getID();
        return TradeContext.create(trader,this.player,this.validator.isThroughNetwork).withCoinSlots(this.coins).withInteractionSlot(this.interactionSlot).withDiscountCodes(this.discountCodes).build();
	}

	protected void init(Inventory inventory) {
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
		}
		
		//Coin Slots
		for(int x = 0; x < this.coins.getSlots(); x++)
		{
			this.coinSlots.add(this.addSlot(new MoneySlot(this.coins, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122)));
		}
		
		//Interaction Slots
		List<InteractionSlotData> slotData = new ArrayList<>();
		for(TraderData trader : this.traderSource.get().getTraders())
			trader.addInteractionSlots(slotData);
		this.interactionSlot = new InteractionSlot(slotData, SLOT_OFFSET + 8, 122);
		this.addSlot(this.interactionSlot);
		
	}

	private boolean traderSourceValid() {  return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().getTraders() != null && !this.traderSource.get().getTraders().isEmpty(); }

    //Subscribe after the trader packet is sent so that the client will also be informed of any network-related changes that may have resulting in the source giving different results
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onServerTick(ServerTickEvent.Post event)
    {
        ITraderSource source = this.traderSource.get();
        Set<Long> found = new HashSet<>();
        if(source != null)
        {
            for(TraderData trader : source.getTraders())
            {
                if(this.trackingCache.contains(trader.getID()))
                    found.add(trader.getID());
                else //Start tracking traders that are now included in the trader source
                    this.trackingHolder.requestTracking(trader,this.player);
            }
        }
        for(long traderID : this.trackingCache)
        {
            //Stop tracking traders that are no longer in the trader source
            if(!found.contains(traderID))
                this.trackingHolder.endTracking(traderID,this.player);
        }
        this.trackingCache = found;
    }

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.clearContainer(player,this.coins);
		this.clearContainer(player,this.interactionSlot.itemHandler);
		if(this.traderSource.get() != null)
		{
			for(TraderData trader : this.traderSource.get().getTraders())
				trader.userClose(this.player);
		}
        this.trackingHolder.clear(player);
        NeoForge.EVENT_BUS.unregister(this);
	}

    @Override
    protected void executeTrade(int traderIndex, int tradeIndex) {
        //LightmansCurrency.LogInfo("Executing trade " + traderIndex + "/" + tradeIndex);
        ITraderSource traderSource = this.traderSource.get();
        if(traderSource == null)
        {
            this.player.closeContainer();
            return;
        }
        List<TraderData> traderList = traderSource.getTraders();
        if(traderIndex >= 0 && traderIndex < traderList.size())
        {
            TraderData trader = traderSource.getTraders().get(traderIndex);
            if(trader == null)
            {
                LightmansCurrency.LogWarning("Trader at index " + traderIndex + " is null.");
                return;
            }
            TradeResult result = trader.TryExecuteTrade(this.getContext(trader), tradeIndex);
            if(result.hasMessage())
                LightmansCurrency.LogDebug(result.getMessage().getString());
        }
        else
            LightmansCurrency.LogWarning("Trader " + traderIndex + " is not a valid trader index.");
    }
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < 36)
			{
				//Move from inventory to coin/interaction slots
				if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				//Move from coin/interaction slots to inventory
				if(!this.moveItemStackTo(slotStack, 0, 36, false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public static class TraderMenuBlockSource extends TraderMenu
	{
		public TraderMenuBlockSource(int windowID, Inventory inventory, BlockPos pos, MenuValidator validator) {
			super(ModMenus.TRADER_BLOCK.get(), windowID, inventory, () -> {
				if(inventory.player.level().getBlockEntity(pos) instanceof ITraderSource source)
					return source;
				return null;
			}, validator);
		}
	}

	public static class TraderMenuAllNetwork extends TraderMenu
	{
		public TraderMenuAllNetwork(int windowID, Inventory inventory, MenuValidator validator) {
			super(ModMenus.TRADER_NETWORK_ALL.get(), windowID, inventory, ITraderSource.NetworkTraderSource(inventory.player.level().isClientSide), validator);
		}
	}
	
	
}
