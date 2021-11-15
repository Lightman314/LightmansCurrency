package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
//import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;

public class ItemTraderUtil {
	
	public static final int TRADEBUTTON_VERT_SPACER = 4;
	public static final int TRADEBUTTON_VERTICALITY = ItemTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_HORIZ_SPACER = 6;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTradeButton.WIDTH + TRADEBUTTON_HORIZ_SPACER;
	
	/*public static int validTradeCount(IItemTrader trader)
	{
		int valid = 0;
		for(int i = 0; i < trader.getTradeCount(); ++i)
			if(trader.getTrade(i).isValid())
				++valid;
		return valid;
	}
	
	public static int getTradeIndexFromValid(IItemTrader trader, int validTradeIndex)
	{
		int currentIndex = 0;
		for(int i = 0; i < trader.getTradeCount(); ++i)
		{
			if(trader.getTrade(i).isValid())
			{
				if(currentIndex == validTradeIndex)
					return i;
				++currentIndex;
			}
		}
		return -1;
	}*/
	
	public static int getWidth(IItemTrader trader)
	{
		return Math.max(176, getTradeDisplayWidth(trader));
	}
	
	public static int getTradeDisplayWidth(IItemTrader trader)
	{
		return 12 + (getTradeDisplayColumnCount(trader) * TRADEBUTTON_HORIZONTAL) - TRADEBUTTON_HORIZ_SPACER;
	}
	
	public static int getTradeDisplayHeight(IItemTrader trader)
	{
		return 17 + (getTradeDisplayRowCount(trader) * TRADEBUTTON_VERTICALITY) + 7;
	}
	
	public static int getTradeDisplayOffset(IItemTrader trader)
	{
		if(getTradeDisplayWidth(trader) > 176)
			return 0;
		return (176 - getTradeDisplayWidth(trader)) / 2;
	}
	
	public static int getInventoryDisplayOffset(IItemTrader trader)
	{
		if(getTradeDisplayWidth(trader) <= 176)
			return 0;
		else
			return (getTradeDisplayWidth(trader) - 176) / 2;
	}
	
	public static int getTradeDisplayColumnCount(IItemTrader trader)
	{
		if(trader.getTradeCount() <= 6)
			return 2;
		else
			return 4;
	}
	
	public static int getTradeDisplayRowCount(IItemTrader trader)
	{
		return ((trader.getTradeCount() - 1)/getTradeDisplayColumnCount(trader)) + 1;
	}
	
	public static int getColumnOf(IItemTrader trader, int validSlotIndex)
	{
		return validSlotIndex % getTradeDisplayColumnCount(trader);
	}
	
	public static int getRowOf(IItemTrader trader, int validSlotIndex)
	{
		return (validSlotIndex / getTradeDisplayColumnCount(trader));
	}
	
	public static int getButtonPosX(IItemTrader trader, int validSlotIndex)
	{
		float offset = 0f;
		/*if(tradeCount == 1 && slotIndex == 0)
		{
			offset = 0.5f;
		}*/
		if(getRowOf(trader, validSlotIndex) == getTradeDisplayRowCount(trader) - 1 && trader.getTradeCount() % getTradeDisplayColumnCount(trader) != 0)
		{
			offset = (0.5f * getTradeDisplayColumnCount(trader)) - (0.5f * (trader.getTradeCount() % getTradeDisplayColumnCount(trader)));
		}
		return (int)(6 + getTradeDisplayOffset(trader) + (((validSlotIndex % getTradeDisplayColumnCount(trader)) + offset) * (ItemTradeButton.WIDTH + 6f)));
	}
	
	public static int getButtonPosY(IItemTrader trader, int validSlotIndex)
	{
		return 17 + (getRowOf(trader, validSlotIndex) * (ItemTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER));
	}
	
	@Deprecated
	public static int getSlotPosX(IItemTrader trader, int validSlotIndex)
	{
		return getButtonPosX(trader, validSlotIndex) + ItemTradeButton.SLOT_OFFSET1_X;
	}
	
	@Deprecated
	public static int getSlotPosY(IItemTrader trader, int validSlotIndex)
	{
		return getButtonPosY(trader, validSlotIndex) + ItemTradeButton.SLOT_OFFSET_Y;
	}
	
}
