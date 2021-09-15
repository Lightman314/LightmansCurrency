package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface ITradeRuleDeserializer<T extends TradeRule> {

	static final Map<String,ITradeRuleDeserializer<?>> registeredDeserializers = new HashMap<>();
	
	public static void RegisterDeserializer(ResourceLocation type, ITradeRuleDeserializer<?> deserializer)
	{
		RegisterDeserializer(type.toString(), deserializer);
	}
	
	public static void RegisterDeserializer(String type, ITradeRuleDeserializer<?> deserializer)
	{
		if(registeredDeserializers.containsKey(type))
		{
			LightmansCurrency.LogWarning("A deserializer of type '" + type + "' has already been registered.");
			return;
		}
		registeredDeserializers.put(type, deserializer);
	}
	
	public static TradeRule Deserialize(CompoundNBT compound)
	{
		String thisType = compound.getString("type");
		AtomicReference<TradeRule> data = new AtomicReference<TradeRule>();
		registeredDeserializers.forEach((type,deserializer) -> {
			if(thisType.equals(type))
			{
				data.set(deserializer.deserialize(compound));
			}
		});
		if(data.get() != null)
			return data.get();
		LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Universal Trader Data from the world save.");
		return null;
	}
	
	public T deserialize(CompoundNBT compound);
	
}
