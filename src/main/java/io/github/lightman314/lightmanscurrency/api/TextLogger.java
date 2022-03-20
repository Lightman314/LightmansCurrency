package io.github.lightman314.lightmanscurrency.api;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.Config;
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
			this.fitToSizeLimit();
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
			//Fit to size limit on load to ensure that changing the config option will correct any log size issues.
			this.fitToSizeLimit();
		}
	}
	
	public final void fitToSizeLimit() {
		while(this.logText.size() > Config.SERVER.logLimit.get() && this.logText.size() > 0)
			this.logText.remove(0);
	}
	
	public static ITextComponent getCostText(CoinValue cost)
	{
		return new StringTextComponent(cost.getString()).mergeStyle(TextFormatting.YELLOW);
	}
	
}
