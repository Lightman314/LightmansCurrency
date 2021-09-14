package io.github.lightman314.lightmanscurrency.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.util.Constants;

public abstract class TextLogger {

	public final List<MutableComponent> logText = new ArrayList<>();
	protected final String tag;
	
	protected TextLogger(String tagName)
	{
		this.tag = tagName;
	}
	
	protected final void clear()
	{
		this.logText.clear();
	}
	
	protected final void AddLog(MutableComponent text)
	{
		if(text != null)
			this.logText.add(text);
	}
	
	public void write(CompoundTag compound)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < logText.size(); i++)
		{
			MutableComponent text = logText.get(i);
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putString("value", TextComponent.Serializer.toJson(text));
			list.add(thisCompound);
		}
		compound.put(this.tag, list);
	}
	
	public void read(CompoundTag compound)
	{
		if(compound.contains(this.tag, Constants.NBT.TAG_LIST))
		{
			ListTag list = compound.getList(this.tag, Constants.NBT.TAG_COMPOUND);
			this.logText.clear();
			for(int i = 0; i < list.size(); i++)
			{
				String jsonText = list.getCompound(i).getString("value");
				MutableComponent text = TextComponent.Serializer.fromJson(jsonText);
				if(text != null)
					this.logText.add(text);
			}
		}
	}
	
}
