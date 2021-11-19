package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IUniversalDataDeserializer<T extends UniversalTraderData> {

	static final Map<ResourceLocation,IUniversalDataDeserializer<?>> registeredDeserializers = new HashMap<>();
	
	public static void RegisterDeserializer(ResourceLocation type, IUniversalDataDeserializer<?> deserializer)
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
		ResourceLocation thisType = new ResourceLocation(compound.getString("type"));
		if(registeredDeserializers.containsKey(thisType))
			return registeredDeserializers.get(thisType).deserialize(compound);
		LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Universal Trader Data from the world save.");
		return null;
	}
	
	public T deserialize(CompoundNBT compound);
	
}
