package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IUniversalDataDeserializer<T extends UniversalTraderData> {

	static final Map<String,IUniversalDataDeserializer<?>> registeredDeserializers = new HashMap<>();
	
	public static void RegisterDeserializer(ResourceLocation type, IUniversalDataDeserializer<?> deserializer)
	{
		RegisterDeserializer(type.toString(), deserializer);
	}
	
	public static void RegisterDeserializer(String type, IUniversalDataDeserializer<?> deserializer)
	{
		if(registeredDeserializers.containsKey(type))
		{
			LightmansCurrency.LogWarning("A deserializer of type '" + type + "' has already been registered.");
			return;
		}
		registeredDeserializers.put(type, deserializer);
	}
	
	public static UniversalTraderData Deserialize(CompoundNBT compound)
	{
		String thisType = compound.getString("type");
		AtomicReference<UniversalTraderData> data = new AtomicReference<UniversalTraderData>();
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
