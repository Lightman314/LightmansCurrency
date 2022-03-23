package io.github.lightman314.lightmanscurrency.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.slots.InteractionSlot;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TraderMenu extends AbstractContainerMenu {

	public final Supplier<ITraderSource> traderSource;
	public final Player player;
	
	
	List<InteractionSlot> interactionSlots = new ArrayList<>();
	Container coinSlots = new SimpleContainer(5);
	
	protected TraderMenu(MenuType<?> type, int windowID, Inventory inventory, BlockPos sourcePosition) {
		super(type, windowID);
		this.player = inventory.player;
		Level level = inventory.player.level;
		this.traderSource = () -> {
			BlockEntity be = level.getBlockEntity(sourcePosition);
			if(be instanceof ITraderSource)
				return (ITraderSource)be;
			return null;
		};
		
		this.init(this.player, inventory);
		
	}
	
	public TradeContext getContext() { 
		return TradeContext.create(this.player).withCoinSlots(this.coinSlots).withInteractionSlots(this.interactionSlots).build();
	}

	protected void init(Player player, Inventory inventory) {
		
	}

	@Override
	public boolean stillValid(Player player) { return this.traderSource != null && this.traderSource.get() != null && this.traderSource.get().getTraders() != null && this.traderSource.get().getTraders().size() > 0; }
	
}
