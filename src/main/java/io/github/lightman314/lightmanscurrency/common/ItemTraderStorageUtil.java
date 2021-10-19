package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;

public class ItemTraderStorageUtil {

	public static final int SCREEN_EXTENSION = ItemTradeButton.WIDTH + 20;
	
	public static int getRowCount(int tradeCount)
	{
		return (tradeCount - 1) / getColumnCount(tradeCount) + 1;
	}
	
	public static int getColumnCount(int tradeCount)
	{
		return tradeCount > 6 ? 2 : 1;
	}
	
	public static int getWidth(int tradeCount)
	{
		return 14 + (getColumnCount(tradeCount) * 162) + 2 * SCREEN_EXTENSION;
	}
	
	public static int getStorageSlotOffset(int tradeCount, int row)
	{
		if(tradeCount % getColumnCount(tradeCount) == 1 && row == getRowCount(tradeCount) - 1)
		{
			return 162 / 2;
		}
		return 0;
	}
	
	public static int getInventoryOffset(int tradeCount)
	{
		if(getColumnCount(tradeCount) > 1)
			return 162/2;
		return 0;
	}
	
	public static boolean isFakeTradeButtonInverted(int tradeCount, int slot)
	{
		if(slot >= getHalfwayPoint(tradeCount)) //Now the right side has them inverted as opposed to the left side
			return true;
		return false;
	}
	
	public static int getHalfwayPoint(int tradeCount)
	{
		if(tradeCount % 2 > 0)
			return (tradeCount / 2) + 1;
		else
			return tradeCount / 2;
	}
	
	public static int getFakeTradeButtonPosX(int tradeCount, int slot)
	{
		if(slot < getHalfwayPoint(tradeCount))
			return 0;
		else
			return getWidth(tradeCount) - SCREEN_EXTENSION + 20;
	}
	
	public static int getFakeTradeButtonPosY(int tradeCount, int slot)
	{
		return (slot % getHalfwayPoint(tradeCount)) * ItemTradeButton.HEIGHT;
	}
	
	public static int getTradePriceButtonPosX(int tradeCount, int slot)
	{
		if(!isFakeTradeButtonInverted(tradeCount, slot))
			return getFakeTradeButtonPosX(tradeCount, slot) + ItemTradeButton.WIDTH;
		return getFakeTradeButtonPosX(tradeCount, slot) - 20;
	}
	
	public static int getTradePriceButtonPosY(int tradeCount, int slot)
	{
		return getFakeTradeButtonPosY(tradeCount, slot) + ((ItemTradeButton.HEIGHT - 20)/2);
	}
	
	public static int getTradeSlotPosX(int tradeCount, int slot)
	{
		if(isFakeTradeButtonInverted(tradeCount, slot))
			return getFakeTradeButtonPosX(tradeCount, slot) + ItemTradeButton.WIDTH - ItemTradeButton.SLOT_OFFSET1_X - 16;
		else
			return getFakeTradeButtonPosX(tradeCount, slot) + ItemTradeButton.SLOT_OFFSET1_X;
	}
	
	public static int getTradeSlotPosY(int tradeCount, int slot)
	{
		return getFakeTradeButtonPosY(tradeCount, slot) + ItemTradeButton.SLOT_OFFSET_Y;
	}
	
}
