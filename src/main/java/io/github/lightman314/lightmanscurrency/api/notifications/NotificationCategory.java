package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public abstract class NotificationCategory implements ITab
{

    public static final Codec<NotificationCategory> CODEC = Codec.withAlternative(
            //Desired Codec
            NotificationCategoryType.CODEC.dispatch(NotificationCategory::getType,NotificationCategoryType::codec),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(NotificationCategory::loadOld,"Notification Category"));

    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationCategory> STREAM_CODEC = NotificationCategoryType.STREAM_CODEC.dispatch(NotificationCategory::getType,NotificationCategoryType::streamCodec);

    public static final NotificationCategory GENERAL = new GeneralCategory();
	public static final NotificationCategoryType<?> GENERAL_TYPE = new NotificationCategoryType.Instance<>(GENERAL);

	public final Component getTooltip() { return this.getName(); }
	public abstract Component getName();
	protected abstract NotificationCategoryType<?> getType();
	public abstract boolean matches(NotificationCategory other);

    private static class GeneralCategory extends NotificationCategory
    {
        @Override
        public IconData getIcon() { return ItemIcon.ofItem(Items.CHEST); }
        @Override
        public Component getName() { return LCText.NOTIFICATION_SOURCE_GENERAL.get(); }
        @Override
        public boolean matches(NotificationCategory other) { return other == GENERAL; }
        @Override
        protected NotificationCategoryType<?> getType() { return GENERAL_TYPE; }
    }
	
	public final CompoundTag save(HolderLookup.Provider lookup) { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public static NotificationCategory load(CompoundTag tag, HolderLookup.Provider lookup) { return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst(); }

    @Deprecated
    private static NotificationCategory loadOld(CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(tag.contains("Type") || tag.contains("type"))
        {
            ResourceLocation type = ResourceLocation.parse(tag.contains("Type") ? tag.getString("Type") : tag.getString("type"));
            if(LCRegistries.NOTIFICATION_CATEGORIES.containsKey(type))
                return LCRegistries.NOTIFICATION_CATEGORIES.get(type).loadOldData(tag, lookup);
            else
            {
                LightmansCurrency.LogError("Cannot load notification category type " + type + " as no NotificationCategoryType has been registered.");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification category as tag is missing the 'type' tag.");
            return null;
        }
    }

	public final boolean notGeneral() { return this != GENERAL; }
	
}
