package io.github.lightman314.lightmanscurrency.api.taxes.notifications.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class TaxEntryCategory extends NotificationCategory {

    public static final NotificationCategoryType<TaxEntryCategory> TYPE = new Type();

    private final long entryID;
    private final Component entryName;
    public Component getEntryName() { return this.entryName; }

    public TaxEntryCategory(Component entryName, long entryID) { this.entryID = entryID; this.entryName = entryName; }

    public TaxEntryCategory(CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(tag.contains("EntryName"))
            this.entryName = Component.Serializer.fromJson(tag.getString("EntryName"), lookup);
        else
            this.entryName = ModBlocks.TAX_COLLECTOR.get().getName();
        if(tag.contains("TraderID"))
            this.entryID = tag.getLong("TraderID");
        else
            this.entryID = -1;
    }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModBlocks.TAX_COLLECTOR); }

    @Override
    public Component getName() { return this.getEntryName(); }

    @Override
    protected NotificationCategoryType<TaxEntryCategory> getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) {
        if(other instanceof TaxEntryCategory otherTax)
            return otherTax.entryID == this.entryID;
        return false;
    }

    public static class Type extends NotificationCategoryType<TaxEntryCategory>
    {

        private static final MapCodec<TaxEntryCategory> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(c -> c.entryName),
                Codec.LONG.fieldOf("id").forGetter(c -> c.entryID)
        ).apply(builder,TaxEntryCategory::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,TaxEntryCategory> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC,c -> c.entryName,
                ByteBufCodecs.VAR_LONG,c -> c.entryID,
                TaxEntryCategory::new);

        @Override
        public MapCodec<TaxEntryCategory> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,TaxEntryCategory> streamCodec() { return STREAM_CODEC; }

        @Override
        public TaxEntryCategory loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return new TaxEntryCategory(tag,lookup); }

    }

}
