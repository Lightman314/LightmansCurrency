package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderCategory extends NotificationCategory {

	public static final NotificationCategoryType<TraderCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("trader"),TraderCategory::new);
	
	private final Item trader;
	private final long traderID;
	private final Component traderName;
    private final IconData traderIcon;

    @Deprecated(since = "2.3.0.0")
	public TraderCategory(ItemLike trader, Component traderName, long traderID) { this(trader,traderName,traderID,IconData.Null()); }
	public TraderCategory(ItemLike trader, Component traderName, long traderID, IconData icon) {
		this.trader = trader.asItem();
		this.traderName = traderName;
		this.traderID = traderID;
        this.traderIcon = icon == null ? IconData.Null() : icon;
	}
	
	public TraderCategory(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("Icon"))
			this.trader = BuiltInRegistries.ITEM.get(VersionUtil.parseResource(compound.getString("Icon")));
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

        if(compound.contains("CustomIcon"))
            this.traderIcon = IconData.load(compound.getCompound("CustomIcon"),lookup);
        else
            this.traderIcon = IconData.Null();
		
	}

	@Override
	public IconData getIcon() {
        if(!this.traderIcon.isNull())
            return this.traderIcon;
        return ItemIcon.ofItem(this.trader);
    }
	@Override
	public Component getName() { return this.traderName; }
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
            else //Invalid trader id, so it's always an invalid match
                return false;
			//Confirm the trader name matches.
			if(!this.traderName.getString().contentEquals(otherTrader.traderName.getString()) || !this.trader.equals(otherTrader.trader))
				return false;
			return true;
		}
		return false;
	}
	
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		compound.putString("Icon", BuiltInRegistries.ITEM.getKey(this.trader).toString());
		compound.putString("TraderName", Component.Serializer.toJson(this.traderName,lookup));
		compound.putLong("TraderID", this.traderID);
        if(!this.traderIcon.isNull())
            compound.put("CustomIcon",this.traderIcon.save(lookup));
	}
	
	
}
