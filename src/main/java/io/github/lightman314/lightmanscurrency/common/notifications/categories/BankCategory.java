package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class BankCategory extends NotificationCategory {

	public static final NotificationCategoryType<BankCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("bank"),BankCategory::new);
	
	private final MutableComponent name;
	
	public BankCategory(MutableComponent name) { this.name = name; }
	
	public BankCategory(CompoundTag compound) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"));
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
			return bc.name.equals(this.name);
		}
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		compound.putString("Name", Component.Serializer.toJson(this.name));
	}
	
}
