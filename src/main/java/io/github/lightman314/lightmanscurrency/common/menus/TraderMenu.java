package io.github.lightman314.lightmanscurrency.common.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class TraderMenu extends EasyMenu implements IValidatedMenu {

	public final Supplier<ITraderSource> traderSource;
	
	public static final int SLOT_OFFSET = 15;
	
	InteractionSlot interactionSlot;
	public InteractionSlot getInteractionSlot() { return this.interactionSlot; }
	Container coins = new SimpleContainer(5);
	public Container getCoinInventory() { return this.coins; }
	
	List<Slot> coinSlots = new ArrayList<>();
	public List<Slot> getCoinSlots() { return this.coinSlots; }

	private final MenuValidator validator;
	@Nonnull
	@Override
	public MenuValidator getValidator() { return this.validator; }

	public TraderMenu(int windowID, Inventory inventory, long traderID, MenuValidator validator) {
		this(ModMenus.TRADER.get(), windowID, inventory, () -> TraderSaveData.GetTrader(inventory.player.level.isClientSide, traderID), validator);
	}
	
	protected TraderMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<ITraderSource> traderSource, MenuValidator validator) {
		super(type, windowID, inventory);
		this.validator = validator;
		this.traderSource = traderSource;

		this.addValidator(this::traderSourceValid);
		this.addValidator(this.validator);

		this.init(inventory);
		for(TraderData trader : this.traderSource.get().getTraders()) {
			if(trader != null) trader.userOpen(this.player);
		}
	}
	
	public TradeContext getContext(TraderData trader) { 
		return TradeContext.create(trader, this.player).withCoinSlots(this.coins).withInteractionSlot(this.interactionSlot).build();
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
		for(int x = 0; x < coins.getContainerSize(); x++)
		{
			this.coinSlots.add(this.addSlot(new CoinSlot(this.coins, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122)));
		}
		
		//Interaction Slots
		List<InteractionSlotData> slotData = new ArrayList<>();
		for(TraderData trader : this.traderSource.get().getTraders())
			trader.addInteractionSlots(slotData);
		this.interactionSlot = new InteractionSlot(slotData, SLOT_OFFSET + 8, 122);
		this.addSlot(this.interactionSlot);
		
	}

	private boolean traderSourceValid() {  return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().getTraders() != null && this.traderSource.get().getTraders().size() > 0; }

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		this.clearContainer(player, this.coins);
		this.clearContainer(player, this.interactionSlot.container);
		if(this.traderSource.get() != null)
		{
			for(TraderData trader : this.traderSource.get().getTraders()) {
				if(trader != null) trader.userClose(this.player);
			}
		}
			
	}
	
	public void ExecuteTrade(int traderIndex, int tradeIndex) {
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
				LightmansCurrency.LogInfo(result.failMessage.getString());
		}
		else
			LightmansCurrency.LogWarning("Trader " + traderIndex + " is not a valid trader index.");
	}
	
	public boolean isSingleTrader() {
		ITraderSource tradeSource = this.traderSource.get();
		if(tradeSource == null)
		{
			this.player.closeContainer();
			return false;
		}
		return tradeSource.isSingleTrader() && tradeSource.getTraders().size() == 1;
	}
	
	public TraderData getSingleTrader() {
		if(this.isSingleTrader())
			return this.traderSource.get().getSingleTrader();
		return null;
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerEntity, int index)
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
	
	public void CollectCoinStorage() {
		if(this.isSingleTrader())
		{
			LightmansCurrency.LogInfo("Attempting to collect coins from trader.");
			TraderData trader = this.getSingleTrader();
			if(trader.hasPermission(this.player, Permissions.COLLECT_COINS))
			{
				CoinValue payment = trader.getInternalStoredMoney();
				if(this.getContext(trader).givePayment(payment))
					trader.clearStoredMoney();
			}
			else
				Permissions.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
		}
	}
	
	public static class TraderMenuBlockSource extends TraderMenu
	{
		public TraderMenuBlockSource(int windowID, Inventory inventory, BlockPos pos, MenuValidator validator) {
			super(ModMenus.TRADER_BLOCK.get(), windowID, inventory, () -> {
				BlockEntity be = inventory.player.level.getBlockEntity(pos);
				if(be instanceof ITraderSource source)
					return source;
				return null;
			}, validator);
		}
	}

	public static class TraderMenuAllNetwork extends TraderMenu
	{
		public TraderMenuAllNetwork(int windowID, Inventory inventory, MenuValidator validator) {
			super(ModMenus.TRADER_NETWORK_ALL.get(), windowID, inventory, ITraderSource.UniversalTraderSource(inventory.player.level.isClientSide), validator);
		}
	}
	
	
}
