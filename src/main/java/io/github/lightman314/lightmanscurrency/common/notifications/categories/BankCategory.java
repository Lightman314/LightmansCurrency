package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BankCategory extends NotificationCategory {

	public static final NotificationCategoryType<BankCategory> TYPE = new NotificationCategoryType<>(VersionUtil.lcResource("bank"),BankCategory::new);
	
	private final Component name;
	
	public BankCategory(Component name) { this.name = name; }
	
	public BankCategory(CompoundTag compound) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"));
	}

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.ATM); }

	@Override
	public Component getName() { return this.name; }

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
