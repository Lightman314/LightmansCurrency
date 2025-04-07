package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public class TraderCategory extends NotificationCategory {

	public static final NotificationCategoryType<TraderCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("trader"),TraderCategory::new);
	
	private final Item trader;
	private final long traderID;
	private final MutableComponent traderName;
	
	public TraderCategory(ItemLike trader, MutableComponent traderName, long traderID) {
		this.trader = trader.asItem();
		this.traderName = traderName;
		this.traderID = traderID;
	}
	
	public TraderCategory(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		if(compound.contains("Icon"))
			this.trader = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compound.getString("Icon")));
		else
			this.trader = ModItems.TRADING_CORE.get();
		
		if(compound.contains("TraderName"))
			this.traderName = Component.Serializer.fromJson(compound.getString("TraderName"),lookup);
		else
			this.traderName = Component.translatable("gui.lightmanscurrency.universaltrader.default");
		
		if(compound.contains("TraderID"))
			this.traderID = compound.getLong("TraderID");
		else
			this.traderID = -1;
		
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(this.trader); }
	
	@Nonnull
	@Override
	public MutableComponent getName() { return this.traderName; }

	@Nonnull
    @Override
	public NotificationCategoryType<TraderCategory> getType() { return TYPE; }
	
	@Override
	public boolean matches(NotificationCategory other) {
		if(other instanceof TraderCategory otherTrader)
		{
			if(this.traderID >= 0)
			{
				//Check if the trader id matches
				if(this.traderID == otherTrader.traderID)
					return true;
			}
			//Confirm the trader name matches.
			if(!this.traderName.getString().contentEquals(otherTrader.traderName.getString()) || !this.trader.equals(otherTrader.trader))
				return false;
			return true;
		}
		return false;
	}
	
	public void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		compound.putString("Icon", BuiltInRegistries.ITEM.getKey(this.trader).toString());
		compound.putString("TraderName", Component.Serializer.toJson(this.traderName,lookup));
		compound.putLong("TraderID", this.traderID);
	}
	
	
}
