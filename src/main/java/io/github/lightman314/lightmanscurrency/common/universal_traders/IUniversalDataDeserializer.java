package io.github.lightman314.lightmanscurrency.common.universal_traders;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

@Deprecated //Use TradingOffice Counterparts
public interface IUniversalDataDeserializer<T extends UniversalTraderData> {

	static final Map<ResourceLocation,IUniversalDataDeserializer<?>> registeredDeserializers = new HashMap<>();
	
	@Deprecated //Use TradingOffice.RegisterDataType
	public static void RegisterDeserializer(ResourceLocation type, IUniversalDataDeserializer<?> deserializer)
	{
		if(registeredDeserializers.containsKey(type))
		{
			LightmansCurrency.LogWarning("A deserializer of type '" + type + "' has already been registered.");
			return;
		}
		registeredDeserializers.put(type, deserializer);
	}
	
	@Deprecated //Use TradingOffice.Deserialize for new deserialization methods
	public static UniversalTraderData Deserialize(CompoundNBT compound)
	{
		return TradingOffice.Deserialize(compound);
	}
	
	@Deprecated //Used in TradingOffice.Deserialize to allow temporary use of deprecated deserializers
	public static UniversalTraderData ClassicDeserialize(CompoundNBT compound)
	{
		ResourceLocation thisType = new ResourceLocation(compound.getString("type"));
		if(registeredDeserializers.containsKey(thisType))
			return registeredDeserializers.get(thisType).deserialize(compound);
		LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Universal Trader Data from the world save.");
		return null;
	}
	
	public T deserialize(CompoundNBT compound);
	
}
