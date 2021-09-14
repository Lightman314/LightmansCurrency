package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
//import io.github.lightman314.lightmanscurrency.util.MathUtil;

public class ItemTraderUtil {
	
	public static final int TRADEBUTTON_VERT_SPACER = 4;
	public static final int TRADEBUTTON_VERTICALITY = ItemTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER;
	public static final int TRADEBUTTON_HORIZ_SPACER = 6;
	public static final int TRADEBUTTON_HORIZONTAL = ItemTradeButton.WIDTH + TRADEBUTTON_HORIZ_SPACER;
	
	public static int getWidth(int tradeCount)
	{
		return Math.max(176, getTradeDisplayWidth(tradeCount));
	}
	
	public static int getTradeDisplayWidth(int tradeCount)
	{
		return 12 + (getTradeDisplayColumnCount(tradeCount) * (TRADEBUTTON_HORIZONTAL)) - TRADEBUTTON_HORIZ_SPACER;
	}
	
	public static int getTradeDisplayHeight(int tradecount)
	{
		return 17 + (getTradeDisplayRowCount(tradecount) * TRADEBUTTON_VERTICALITY) + 7;
	}
	
	public static int getTradeDisplayOffset(int tradeCount)
	{
		if(getTradeDisplayWidth(tradeCount) > 176)
			return 0;
		return (176 - getTradeDisplayWidth(tradeCount)) / 2;
	}
	
	public static int getInventoryDisplayOffset(int tradeCount)
	{
		if(getTradeDisplayWidth(tradeCount) <= 176)
			return 0;
		else
			return (getTradeDisplayWidth(tradeCount) - 176) / 2;
	}
	
	public static int getTradeDisplayColumnCount(int tradeCount)
	{
		if(tradeCount <= 6)
			return 2;
		else
			return 4;
	}
	
	public static int getTradeDisplayRowCount(int tradeCount)
	{
		return ((tradeCount - 1)/getTradeDisplayColumnCount(tradeCount)) + 1;
	}
	
	public static int getColumnOf(int tradeCount, int slotIndex)
	{
		return slotIndex % getTradeDisplayColumnCount(tradeCount);
	}
	
	public static int getRowOf(int tradeCount, int slotIndex)
	{
		return (slotIndex / getTradeDisplayColumnCount(tradeCount));
	}
	
	public static int getButtonPosX(int tradeCount, int slotIndex)
	{
		float offset = 0f;
		/*if(tradeCount == 1 && slotIndex == 0)
		{
			offset = 0.5f;
		}*/
		if(getRowOf(tradeCount, slotIndex) == getTradeDisplayRowCount(tradeCount) - 1 && tradeCount % getTradeDisplayColumnCount(tradeCount) != 0)
		{
			offset = (0.5f * getTradeDisplayColumnCount(tradeCount)) - (0.5f * (tradeCount % getTradeDisplayColumnCount(tradeCount)));
		}
		return (int)(6 + getTradeDisplayOffset(tradeCount) + (((slotIndex % getTradeDisplayColumnCount(tradeCount)) + offset) * (ItemTradeButton.WIDTH + 6f)));
	}
	
	public static int getButtonPosY(int tradeCount, int slotIndex)
	{
		return 17 + (getRowOf(tradeCount, slotIndex) * (ItemTradeButton.HEIGHT + TRADEBUTTON_VERT_SPACER));
	}
	
	public static int getSlotPosX(int tradeCount, int slotIndex)
	{
		return getButtonPosX(tradeCount, slotIndex) + ItemTradeButton.SLOT_OFFSET_X;
	}
	
	public static int getSlotPosY(int tradeCount, int slotIndex)
	{
		return getButtonPosY(tradeCount, slotIndex) + ItemTradeButton.SLOT_OFFSET_Y;
	}
	
}
