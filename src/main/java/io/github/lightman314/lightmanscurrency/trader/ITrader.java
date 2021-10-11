package io.github.lightman314.lightmanscurrency.trader;

import java.util.UUID;

import net.minecraft.util.text.ITextComponent;

public interface ITrader {

	public UUID getOwnerID();
	public boolean hasCustomName();
	public ITextComponent getName();
	
}
