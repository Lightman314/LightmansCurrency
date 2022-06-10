package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public class TraderCategory extends Category {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID,"trader");
	
	private final Item trader;
	private final MutableComponent traderName;
	public MutableComponent getTraderName() { return this.traderName; }
	
	public TraderCategory(ItemLike trader, MutableComponent traderName) {
		this.trader = trader.asItem();
		this.traderName = traderName;
	}
	
	public TraderCategory(CompoundTag compound) {
		
		if(compound.contains("Icon"))
			this.trader = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("Icon")));
		else
			this.trader = ModItems.TRADING_CORE.get();
		
		if(compound.contains("TraderName"))
			this.traderName = Component.Serializer.fromJson(compound.getString("TraderName"));
		else
			this.traderName = Component.translatable("gui.lightmanscurrency.universaltrader.default");
		
	}

	@Override
	public IconData getIcon() { return IconData.of(this.trader); }
	
	@Override
	public MutableComponent getName() { return this.traderName; }

	@Override
	public ResourceLocation getType() { return TYPE; }
	
	@Override
	public boolean matches(Category other) {
		if(other instanceof TraderCategory)
		{
			TraderCategory otherTrader = (TraderCategory)other;
			//Confirm the trader name matches.
			if(!this.traderName.getString().contentEquals(otherTrader.traderName.getString()) || !this.trader.equals(otherTrader.trader))
				return false;
			return true;
		}
		return false;
	}
	
	public void saveAdditional(CompoundTag compound) {
		compound.putString("Icon", ForgeRegistries.ITEMS.getKey(this.trader).toString());
		compound.putString("TraderName", Component.Serializer.toJson(this.traderName));
	}
	
	
}
