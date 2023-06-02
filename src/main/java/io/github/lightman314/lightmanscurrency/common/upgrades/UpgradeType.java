package io.github.lightman314.lightmanscurrency.common.upgrades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class UpgradeType {

	private static final Map<ResourceLocation,UpgradeType> UPGRADE_TYPE_REGISTRY = new HashMap<>();
	
	public static final ItemCapacityUpgrade ITEM_CAPACITY = register(new ResourceLocation(LightmansCurrency.MODID, "item_capacity"), new ItemCapacityUpgrade());
	
	public static final SpeedUpgrade SPEED = register(new ResourceLocation(LightmansCurrency.MODID, "speed"), new SpeedUpgrade());
	
	public static final Simple NETWORK = register(new ResourceLocation(LightmansCurrency.MODID, "trader_network"), new Simple(Component.translatable("tooltip.lightmanscurrency.upgrade.network")));
	
	public static final Simple HOPPER = register(new ResourceLocation(LightmansCurrency.MODID, "hopper"), new Simple(Component.translatable("tooltip.lightmanscurrency.upgrade.hopper")));

	//Coin Chest Upgrades
	public static final CoinChestExchangeUpgrade COIN_CHEST_EXCHANGE = register(new ResourceLocation(LightmansCurrency.MODID, "coin_chest_exchange"), new CoinChestExchangeUpgrade());
	public static final CoinChestBankUpgrade COIN_CHEST_BANK = register(new ResourceLocation(LightmansCurrency.MODID, "coin_chest_bank"), new CoinChestBankUpgrade());
	public static final CoinChestMagnetUpgrade COIN_CHEST_MAGNET = register(new ResourceLocation(LightmansCurrency.MODID, "coin_chest_magnet"), new CoinChestMagnetUpgrade());
	public static final CoinChestSecurityUpgrade COIN_CHEST_SECURITY = register(new ResourceLocation(LightmansCurrency.MODID, "coin_chest_security"), new CoinChestSecurityUpgrade());


	private ResourceLocation type;
	
	protected abstract List<String> getDataTags();
	protected abstract Object defaultTagValue(String tag);
	public List<Component> getTooltip(UpgradeData data) { return Lists.newArrayList(); }
	public final UpgradeData getDefaultData() { return new UpgradeData(this); }

	public final UpgradeType setRegistryName(ResourceLocation name) {
		this.type = name;
		return this;
	}

	public ResourceLocation getRegistryName() {
		return this.type;
	}

	public Class<UpgradeType> getRegistryType() {
		return UpgradeType.class;
	}

	public void clearDataFromStack(CompoundTag itemTag) {}

	private static <T extends UpgradeType> T register(ResourceLocation type, T upgradeType)
	{
		UPGRADE_TYPE_REGISTRY.put(type, upgradeType.setRegistryName(type));
		return upgradeType;
	}
	
	public interface IUpgradeable
	{
		default boolean allowUpgrade(UpgradeItem item) { return this.allowUpgrade(item.getUpgradeType()); }
		boolean allowUpgrade(UpgradeType type);
	}
	
	public interface IUpgradeItem
	{
		UpgradeType getUpgradeType();
		UpgradeData getDefaultUpgradeData();
		default void onApplied(IUpgradeable target) { }
	}

	public static class UpgradeData
	{

		public static final UpgradeData EMPTY = new UpgradeData();

		private final Map<String,Object> data = new HashMap<>();
		
		public Set<String> getKeys() { return data.keySet(); }
		
		public boolean hasKey(String tag)
		{
			return this.getKeys().contains(tag);
		}

		private UpgradeData() {}

		public UpgradeData(UpgradeType upgrade)
		{
			for(String tag : upgrade.getDataTags())
			{
				Object defaultValue = upgrade.defaultTagValue(tag);
				data.put(tag, defaultValue);
			}
		}
		
		public void setValue(String tag, Object value)
		{
			if(data.containsKey(tag))
				data.put(tag, value);
		}
		
		public Object getValue(String tag)
		{
			if(data.containsKey(tag))
				return data.get(tag);
			return null;
		}

		public boolean getBooleanValue(String tag)
		{
			if(getValue(tag) instanceof Boolean b)
				return b;
			return false;
		}

		public int getIntValue(String tag)
		{
			if(getValue(tag) instanceof Integer i)
				return i;
			return 0;
		}

		public long getLongValue(String tag)
		{
			if(getValue(tag) instanceof Long l)
				return l;
			return 0;
		}
		
		public float getFloatValue(String tag)
		{
			if(getValue(tag) instanceof Float f)
				return f;
			return 0f;
		}
		
		public String getStringValue(String tag)
		{
			if(getValue(tag) instanceof String s)
				return s;
			return "";
		}

		public CompoundTag getCompoundValue(String tag)
		{
			if(getValue(tag) instanceof CompoundTag c)
				return c;
			return new CompoundTag();
		}
		
		public void read(CompoundTag compound)
		{
			compound.getAllKeys().forEach(key ->{
				if(this.hasKey(key))
				{
					if(compound.contains(key, Tag.TAG_BYTE))
						this.setValue(key, compound.getBoolean(key));
					else if(compound.contains(key, Tag.TAG_INT))
						this.setValue(key, compound.getInt(key));
					else if(compound.contains(key, Tag.TAG_LONG))
						this.setValue(key, compound.getLong(key));
					else if(compound.contains(key, Tag.TAG_FLOAT))
						this.setValue(key, compound.getFloat(key));
					else if(compound.contains(key, Tag.TAG_STRING))
						this.setValue(key, compound.getString(key));
					else if(compound.contains(key, Tag.TAG_COMPOUND))
						this.setValue(key, compound.getCompound(key));
				}
			});
		}
		
		public CompoundTag writeToNBT() { return writeToNBT(null); }
		
		public CompoundTag writeToNBT(@Nullable UpgradeType source)
		{
			Map<String,Object> modifiedEntries = source == null ? this.data : getModifiedEntries(this,source);
			CompoundTag compound = new CompoundTag();
			modifiedEntries.forEach((key,value) ->{
				if(value instanceof Boolean)
					compound.putBoolean(key,(Boolean)value);
				if(value instanceof Integer)
					compound.putInt(key, (Integer)value);
				else if(value instanceof Float)
					compound.putFloat(key, (Float)value);
				else if(value instanceof Long)
					compound.putLong(key, (Long)value);
				else if(value instanceof String)
					compound.putString(key, (String)value);
				else if(value instanceof CompoundTag)
					compound.put(key, (CompoundTag)value);
			});
			return compound;
		}
		
		public static Map<String,Object> getModifiedEntries(UpgradeData queryData, UpgradeType source)
		{
			Map<String,Object> modifiedEntries = Maps.newHashMap();
			source.getDefaultData().data.forEach((key, value) -> {
				if(queryData.data.containsKey(key) && !Objects.equal(queryData.data.get(key), value))
						modifiedEntries.put(key, value);
			});
			return modifiedEntries;
		}
		
	}
	
	public static class RegisterUpgradeTypeEvent extends Event{
		
		public RegisterUpgradeTypeEvent() { }
		
		public <T extends UpgradeType> void Register(ResourceLocation type, T upgradeType) { register(type, upgradeType); }
		
	}
	
	public static boolean hasUpgrade(UpgradeType type, Container upgradeContainer) {
		for(int i = 0; i < upgradeContainer.getContainerSize(); ++i)
		{
			ItemStack stack = upgradeContainer.getItem(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(upgradeItem.getUpgradeType() == type)
					return true;
			}
		}
		return false;
	}
	
	public static class Simple extends UpgradeType {

		private final List<Component> tooltips;
		public Simple(Component... tooltips) { this.tooltips = Lists.newArrayList(tooltips); }
		
		@Override
		protected List<String> getDataTags() { return new ArrayList<>(); }

		@Override
		protected Object defaultTagValue(String tag) { return null; }
		
		@Override
		public List<Component> getTooltip(UpgradeData data) { return this.tooltips; }
		
	}
	
}
