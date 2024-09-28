package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BankCategory extends NotificationCategory {

	public static final NotificationCategoryType<BankCategory> TYPE = new NotificationCategoryType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "bank"),BankCategory::new);
	
	private final MutableComponent name;
	
	public BankCategory(MutableComponent name) { this.name = name; }
	
	public BankCategory(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"),lookup);
	}

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.ATM); }

	@Nonnull
	@Override
	public MutableComponent getName() { return this.name; }

	@Nonnull
    @Override
	protected NotificationCategoryType<BankCategory> getType() { return TYPE; }

	@Override
	public boolean matches(NotificationCategory other) {
		if(other instanceof BankCategory bc)
		{
			return bc.name.getString().equals(this.name.getString());
		}
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		compound.putString("Name", Component.Serializer.toJson(this.name,lookup));
	}
	
}
