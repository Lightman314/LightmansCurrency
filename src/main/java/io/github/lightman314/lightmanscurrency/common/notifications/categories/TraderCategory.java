package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class TraderCategory extends NotificationCategory {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"trader");
	
	private final Item trader;
	private final long traderID;
	private final MutableComponent traderName;
	public MutableComponent getTraderName() { return this.traderName; }
	
	public TraderCategory(ItemLike trader, MutableComponent traderName, long traderID) {
		this.trader = trader.asItem();
		this.traderName = traderName;
		this.traderID = traderID;
	}
	
	public TraderCategory(CompoundTag compound) {
		
		if(compound.contains("Icon"))
			this.trader = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("Icon")));
		else
			this.trader = ModItems.TRADING_CORE.get();
		
		if(compound.contains("TraderName"))
			this.traderName = EasyText.Serializer.fromJson(compound.getString("TraderName"));
		else
			this.traderName = EasyText.translatable("gui.lightmanscurrency.universaltrader.default");
		
		if(compound.contains("TraderID"))
			this.traderID = compound.getLong("TraderID");
		else
			this.traderID = -1;
		
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(this.trader); }
	
	@Override
	public MutableComponent getName() { return this.traderName; }

	@Override
	public ResourceLocation getType() { return TYPE; }
	
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
	
	public void saveAdditional(CompoundTag compound) {
		compound.putString("Icon", ForgeRegistries.ITEMS.getKey(this.trader).toString());
		compound.putString("TraderName", EasyText.Serializer.toJson(this.traderName));
		compound.putLong("TraderID", this.traderID);
	}
	
	
}
