package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatisticsDataHolder;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStatListeningNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class TraderStatsNode extends SimpleSyncedNode implements IStatListeningNode, ISettingsMessageListener {

    private static final MapCodec<TraderStatsNode> MAP_CODEC = StatisticsDataHolder.CODEC.fieldOf("stats").xmap(TraderStatsNode::new,s -> s.stats);

    public static final TraderNodeType<TraderStatsNode> TYPE = TraderNodeType.simple(TraderStatsNode::new,MAP_CODEC);

    public static final TextEntry TOOLTIP_TRADER_STATS = TextEntry.tooltip(LCApi.MODID,"trader.stats");

    private final StatisticsDataHolder stats;
    public StatHolder.Clearable getStats() { return this.stats; }

    private TraderStatsNode() { this(new StatisticsDataHolder()); }
    private TraderStatsNode(StatisticsDataHolder stats) { this.stats = stats.withListener(this::onStatsChanged); }

    private void onStatsChanged(Consumer<FancyPacketMap.Mutable> change) {
        this.setChanged(change);
    }

    @Override
    public TrackingLevel requiredTrackingLevel() { return TrackingLevel.STORAGE; }
    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        this.stats.writePacket(builder);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        this.stats.handlePacket(data);
    }

    @Override
    public <V, A> void afterStatAdded(StatKey<V, A> key, A addValue) {
        this.stats.addToStat(key,addValue);
    }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("clearStats") && this.getPermission(player,LCPermissions.VIEW_LOGS).hasHigherPermission())
            this.stats.clear(message.getBoolean("clearStats"));
    }
}
