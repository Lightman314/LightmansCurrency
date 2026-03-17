package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class OutOfStockNotification extends SingleLineNotification {

	public static final NotificationType<OutOfStockNotification> TYPE = new Type();
	
	TraderCategory traderData = TraderCategory.getEmpty();
	
	int tradeSlot;

	private OutOfStockNotification() { }
    private OutOfStockNotification(TraderCategory trader, int tradeIndex, CommonData data) {
        super(data);
        this.traderData = trader;
        this.tradeSlot = tradeIndex;
    }
	protected OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
		this.traderData = traderData;
		this.tradeSlot = tradeIndex + 1;
	}

	public static Supplier<Notification> create(TraderCategory trader, int tradeIndex) { return () -> new OutOfStockNotification(trader, tradeIndex); }

    @Override
	public NotificationType<OutOfStockNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public Component getMessage() { return this.tradeSlot > 0 ? LCText.NOTIFICATION_TRADER_OUT_OF_STOCK.get(this.traderData.getTooltip(), this.tradeSlot) : LCText.NOTIFICATION_TRADER_OUT_OF_STOCK_INDEXLESS.get(); }

	@Override
    @Deprecated
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.traderData = TraderCategory.loadOldData(compound.getCompound("TraderInfo"),lookup);
		this.tradeSlot = compound.getInt("TradeSlot");
	}

	@Override
	protected boolean canMerge(Notification other) { return false; }

    private static class Type extends NotificationType<OutOfStockNotification>
    {
        private static final MapCodec<OutOfStockNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().fieldOf("trader").forGetter(n -> n.traderData),
                Codec.INT.fieldOf("slot").forGetter(n -> n.tradeSlot),
                baseFields()
        ).apply(builder,OutOfStockNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,OutOfStockNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                ByteBufCodecs.INT,n -> n.tradeSlot,
                OutOfStockNotification::new);

        @Override
        protected OutOfStockNotification createNew() { return new OutOfStockNotification(); }
        @Override
        public MapCodec<OutOfStockNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OutOfStockNotification> streamCodec() { return STREAM_CODEC; }
    }

}
