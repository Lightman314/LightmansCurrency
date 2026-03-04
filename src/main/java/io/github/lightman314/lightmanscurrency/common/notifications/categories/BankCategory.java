package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class BankCategory extends NotificationCategory {

	public static final NotificationCategoryType<BankCategory> TYPE = new Type();
	
	private final Component name;
	
	public BankCategory(Component name) { this.name = name; }
	
	public BankCategory(CompoundTag compound, HolderLookup.Provider lookup) {
		this.name = Component.Serializer.fromJson(compound.getString("Name"),lookup);
	}

	@Override
	public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.ATM); }

	@Override
	public Component getName() { return EasyText.makeMutable(this.name); }

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

    private static class Type extends NotificationCategoryType<BankCategory>
    {

        private static final MapCodec<BankCategory> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(c -> c.name))
                .apply(builder,BankCategory::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,BankCategory> STREAM_CODEC = ComponentSerialization.STREAM_CODEC.map(BankCategory::new,c -> c.name);

        @Override
        public MapCodec<BankCategory> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BankCategory> streamCodec() { return STREAM_CODEC; }
        @Override
        public BankCategory loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return new BankCategory(tag,lookup); }
    }
	
}
