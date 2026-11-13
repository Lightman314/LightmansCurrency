package io.github.lightman314.lightmanscurrency.features.trader.paygate.menu;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.AdvancedTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.trade.PaygateTradeData;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class PaygateTradeEditTab extends AdvancedTradeEditTab {

    public static final Identifier KEY = LCApi.id("advanced_paygate_trade_edit");

    public PaygateTradeEditTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }

    public void setDuration(int duration) {
        if(this.getPermission(LCPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof PaygateTradeData trade) {
            trade.setDuration(duration);
            if(this.isClient())
                this.send(FancyPacketMap.map().setInt("duration",duration));
        }
    }

    public void setLevel(int level) {
        if(this.getPermission(LCPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof PaygateTradeData trade) {
            trade.setRedstoneLevel(level);
            if(this.isClient())
                this.send(FancyPacketMap.map().setInt("level",level));
        }
    }

    public void setOutputSide(Direction side,DirectionalSettingsState state) {
        if(this.getPermission(LCPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof PaygateTradeData trade) {
            if(state.allowsInputs())
                return;
            trade.getOutputSides().setState(side,state);
            if(this.isClient())
                this.send(FancyPacketMap.map()
                        .setMap("output",FancyPacketMap.map()
                                .setEnum("side",side)
                                .setEnum("state",state)));
        }
    }

    public void setDescription(String description) {
        if(this.getPermission(LCPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof PaygateTradeData trade) {
            trade.setDescription(description);
            if(this.isClient())
                this.send(FancyPacketMap.map().setString("description",description));
        }
    }

    public void setTooltip(String tooltip) {
        if(this.getPermission(LCPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof PaygateTradeData trade) {
            trade.setTooltip(tooltip);
            if(this.isClient())
                this.send(FancyPacketMap.map().setString("tooltip",tooltip));
        }
    }

    @Override
    public void handleMessage(FancyPacketMap packet) {
        super.handleMessage(packet);
        if(packet.contains("duration"))
            this.setDuration(packet.getInt("duration"));
        if(packet.contains("level"))
            this.setLevel(packet.getInt("level"));
        if(packet.contains("output")) {
            FancyPacketMap entry = packet.getMap("output");
            Direction side = entry.getEnum("side",Direction.class);
            DirectionalSettingsState state = entry.getEnum("state",DirectionalSettingsState.class);
            if(side != null && state != null)
                this.setOutputSide(side,state);
        }
        if(packet.contains("description"))
            this.setDescription(packet.getString("description"));
        if(packet.contains("tooltip"))
            this.setTooltip(packet.getString("tooltip"));
    }
}
