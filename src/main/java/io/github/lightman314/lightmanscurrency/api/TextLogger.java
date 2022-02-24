package io.github.lightman314.lightmanscurrency.api;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

public abstract class TextLogger {

	public final List<ITextComponent> logText = new ArrayList<>();
	protected final String tag;
	
	protected TextLogger(String tagName)
	{
		this.tag = tagName;
	}
	
	public final void clear()
	{
		this.logText.clear();
	}
	
	protected final void AddLog(ITextComponent text)
	{
		if(text != null)
		{
			this.logText.add(text);
		}
	}
	
	public void write(CompoundNBT compound)
	{
		ListNBT list = new ListNBT();
		for(int i = 0; i < logText.size(); i++)
		{
			ITextComponent text = logText.get(i);
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("value", ITextComponent.Serializer.toJson(text));
			list.add(thisCompound);
		}
		compound.put(this.tag, list);
	}
	
	public void read(CompoundNBT compound)
	{
		if(compound.contains(this.tag, Constants.NBT.TAG_LIST))
		{
			ListNBT list = compound.getList(this.tag, Constants.NBT.TAG_COMPOUND);
			this.logText.clear();
			for(int i = 0; i < list.size(); i++)
			{
				String jsonText = list.getCompound(i).getString("value");
				ITextComponent text = ITextComponent.Serializer.getComponentFromJson(jsonText);
				if(text != null)
					this.logText.add(text);
			}
		}
	}
	
	public static ITextComponent getCostText(CoinValue cost)
	{
		return new StringTextComponent(cost.getString()).mergeStyle(TextFormatting.YELLOW);
	}
	
}
