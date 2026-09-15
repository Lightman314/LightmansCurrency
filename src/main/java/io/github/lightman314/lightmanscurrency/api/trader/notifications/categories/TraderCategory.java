package io.github.lightman314.lightmanscurrency.api.trader.notifications.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class TraderCategory extends NotificationCategory {

    private static final MapCodec<TraderCategory> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.LONG.fieldOf("traderID").forGetter(c -> c.traderID),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(TraderCategory::getName),
            IconData.CODEC.fieldOf("icon").forGetter(TraderCategory::getIcon)
    ).apply(builder,TraderCategory::new));
    public static final Codec<TraderCategory> TRADER_CATEGORY_CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf,TraderCategory> TRADER_CATEGORY_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,c -> c.traderID,
            ComponentSerialization.STREAM_CODEC,TraderCategory::getName,
            IconData.STREAM_CODEC,TraderCategory::getIcon,
            TraderCategory::new);

    public static final NotificationCategoryType<TraderCategory> TYPE = new NotificationCategoryType<>(MAP_CODEC,TRADER_CATEGORY_STREAM_CODEC);

    private final long traderID;
    private Component lastKnownName;
    private IconData lastKnownIcon;

    public TraderCategory(TraderData trader) {
        this.traderID = trader.getID();
        this.lastKnownName = IDisplayNode.getTraderName(trader);
        this.lastKnownIcon = extractIcon(trader);
    }
    public TraderCategory(long traderID,Component lastKnownName,IconData lastKnownIcon) {
        this.traderID = traderID;
        this.lastKnownName = lastKnownName;
        this.lastKnownIcon = lastKnownIcon;
    }

    @Override
    public Component getName() {
        //Don't attempt to get the display data from the trader on the client as it may not be synced
        if(this.isClient())
            return this.lastKnownName;
        TraderData trader = LCApi.getTraderAPI().getTrader(this,this.traderID);
        if(trader != null)
            this.lastKnownName = IDisplayNode.getTraderName(trader);
        return this.lastKnownName;
    }

    @Override
    public IconData getIcon() {
        //Don't attempt to get the display data from the trader on the client as it may not be synced
        if(this.isClient())
            return this.lastKnownIcon;
        TraderData trader = LCApi.getTraderAPI().getTrader(this,this.traderID);
        if(trader != null)
            this.lastKnownIcon = extractIcon(trader);
        return this.lastKnownIcon;
    }

    private static IconData extractIcon(TraderData trader) {
        //If the trader has a custom icon, use that icon
        Optional<IconData> optional = IDisplayNode.getCustomTraderIcon(trader);
        if(optional.isPresent())
            return optional.get();
        //Then default to the traders block as an icon
        if(trader.hasNode(WorldNode.TYPE)) {
            WorldNode node = trader.getNode(WorldNode.TYPE);
            if(node != null && node.getBlock() != Blocks.AIR)
                return ItemIcon.of(node.getBlock());
        }
        //Otherwise use the default icon
        return IDisplayNode.getDefaultTraderIcon(trader).orElse(ItemIcon.of(LCItems.TRADING_CORE));
    }

    @Override
    public NotificationCategoryType<?> getType() { return TYPE; }

    @Override
    public boolean equals(NotificationCategory other) {
        if(other instanceof TraderCategory tc)
            return tc.traderID == this.traderID;
        return false;
    }

    @Override
    public int hashCode() { return (int)this.traderID; }

}
