package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.trader.common.InteractionSlotData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;

public class InteractionSlot extends Slot{

	public final InteractionSlotData slotData;
	
	public InteractionSlot(InteractionSlotData slotData, int x, int y) {
		super(new SimpleContainer(1), 0, x, y);
		this.slotData = slotData;
	}

	@Nullable
	public static InteractionSlot getSlot(List<InteractionSlot> slots, String type) {
		for(InteractionSlot slot : slots)
		{
			if(slot.slotData.type.contentEquals(type))
				return slot;
		}
		return null;
	}
	
}
