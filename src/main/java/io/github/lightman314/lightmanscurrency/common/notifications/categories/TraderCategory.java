package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.jarjar.nio.util.Lazy;

import javax.annotation.Nullable;
import java.util.Optional;

public class TraderCategory extends NotificationCategory {

    private static final Lazy<TraderCategory> EMPTY = Lazy.of(() -> new TraderCategory(ModBlocks.DISPLAY_CASE.get(Color.WHITE),EasyText.empty(),-1,Optional.empty()));
    public static TraderCategory getEmpty() { return EMPTY.get(); }

	public static final NotificationCategoryType<TraderCategory> TYPE = new Type();
	
	private final Item trader;
	private final long traderID;
	private final Component traderName;
    private final Optional<IconData> traderIcon;

    @Deprecated(since = "2.3.0.0")
	public TraderCategory(ItemLike trader, Component traderName, long traderID) { this(trader,traderName,traderID,Optional.empty()); }
	public TraderCategory(ItemLike trader, Component traderName, long traderID, @Nullable IconData icon) { this(trader,traderName,traderID,Optional.ofNullable(icon)); }
	public TraderCategory(ItemLike trader, Component traderName, long traderID, Optional<IconData> icon) {
		this.trader = trader.asItem();
		this.traderName = traderName;
		this.traderID = traderID;
        this.traderIcon = icon;
	}

    @Deprecated
	private TraderCategory(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("Icon"))
			this.trader = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compound.getString("Icon")));
		else
			this.trader = ModItems.TRADING_CORE.get();
		
		if(compound.contains("TraderName"))
			this.traderName = Component.Serializer.fromJson(compound.getString("TraderName"),lookup);
		else
			this.traderName = LCText.GUI_TRADER_DEFAULT_NAME.get();
		
		if(compound.contains("TraderID"))
			this.traderID = compound.getLong("TraderID");
		else
			this.traderID = -1;

        if(compound.contains("CustomIcon"))
            this.traderIcon = Optional.ofNullable(IconData.loadOldData(compound.getCompound("CustomIcon"),lookup));
        else
            this.traderIcon = Optional.empty();

	}

    @Deprecated
    public static TraderCategory loadOldData(CompoundTag compoundTag, HolderLookup.Provider lookup) { return new TraderCategory(compoundTag,lookup); }

	@Override
	public IconData getIcon() {
        if(this.traderIcon.isPresent())
            return this.traderIcon.get();
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

    private static class Type extends NotificationCategoryType<TraderCategory>
    {
        private static final MapCodec<TraderCategory> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(c -> c.trader),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(c -> c.traderName),
                Codec.LONG.fieldOf("id").forGetter(c -> c.traderID),
                IconData.CODEC.optionalFieldOf("icon").forGetter(c -> c.traderIcon)
        ).apply(builder,TraderCategory::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,TraderCategory> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM),c -> c.trader,
                ComponentSerialization.STREAM_CODEC,c -> c.traderName,
                ByteBufCodecs.VAR_LONG,c -> c.traderID,
                ByteBufCodecs.optional(IconData.STREAM_CODEC),c -> c.traderIcon,
                TraderCategory::new);

        @Override
        public MapCodec<TraderCategory> codec() { return CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TraderCategory> streamCodec() { return STREAM_CODEC; }

        @Override
        public TraderCategory loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return new TraderCategory(tag,lookup); }
    }
	
}
