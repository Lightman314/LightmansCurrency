package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.List;

public class AdminSettingsNode extends SimpleSyncedNode implements IAdminSettingProvider, ISettingsMessageListener, ISettingsStorageIONode {

    private static final MapCodec<AdminSettingsNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("unlimitedStock").forGetter(AdminSettingsNode::hasUnlimitedStock),
            Codec.BOOL.fieldOf("storePrice").forGetter(AdminSettingsNode::shouldStorePrice)
    ).apply(builder,AdminSettingsNode::new));
    public static final TraderNodeType<AdminSettingsNode> TYPE = TraderNodeType.simple(AdminSettingsNode::new,MAP_CODEC);

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_UNLIMITED_STOCK = TextEntry.traderNodeValue(TYPE,"unlimited_stock");
    public static final TextEntry VALUE_STORE_PRICE = TextEntry.traderNodeValue(TYPE,"store_price");

    private boolean unlimitedStock = false;
    public boolean hasUnlimitedStock() { return this.unlimitedStock; }
    public void setUnlimitedStock(boolean newState,@Nullable PlayerReference admin) {
        if(this.unlimitedStock != newState) {
            this.unlimitedStock = newState;
            this.setChanged(builder -> builder.setBoolean("unlimitedStock",this.unlimitedStock));
            if(admin != null)
                this.postInternalNotification(ChangeSettingNotification.simple(admin,VALUE_UNLIMITED_STOCK.get(),this.unlimitedStock));
        }
    }
    private boolean storePrice = true;
    public boolean shouldStorePrice() { return this.storePrice; }
    public void setStorePrice(boolean newState,@Nullable PlayerReference admin) {
        if(this.storePrice != newState) {
            this.storePrice = newState;
            this.setChanged(builder -> builder.setBoolean("storePrice",this.storePrice));
            if(admin != null)
                this.postInternalNotification(ChangeSettingNotification.simple(admin,VALUE_STORE_PRICE.get(),this.storePrice));
        }
    }

    private AdminSettingsNode() {}
    private AdminSettingsNode(boolean unlimitedStock,boolean storePrice) {
        this.unlimitedStock = unlimitedStock;
        this.storePrice = storePrice;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setBoolean("unlimitedStock",this.unlimitedStock)
                .setBoolean("storePrice",this.storePrice);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        this.unlimitedStock = data.getBoolean("unlimitedStock",this.unlimitedStock);
        this.storePrice = data.getBoolean("storePrice",this.storePrice);
    }

    @Override
    public void modifyInfiniteStock(MutableBoolean currentState) {
        if(this.unlimitedStock)
            currentState.setTrue();
    }
    @Override
    public void modifyStorePrice(MutableBoolean currentState) {
        if(!this.storePrice)
            currentState.setFalse();
    }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("unlimitedStock") && LCApi.isInAdminMode(player))
            this.setUnlimitedStock(message.getBoolean("unlimitedStock"),PlayerReference.of(player));
        if(message.contains("storePrice") && LCApi.isInAdminMode(player))
            this.setStorePrice(message.getBoolean("storePrice"),PlayerReference.of(player));
    }

    @Override
    public List<ISettingsStorageIO> getEntries(Player player) {
        //Only make this visible if the player is in admin mode
        if(LCApi.isInAdminMode(player))
            return ISettingsStorageIONode.super.getEntries(player);
        return List.of();
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putBoolean("unlimitedStock",this.unlimitedStock);
        output.putBoolean("storePrice",this.storePrice);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        if(context.isAdminPlayer()) {
            this.setUnlimitedStock(data.getBooleanOr("unlimitedStock",this.unlimitedStock),null);
            this.setStorePrice(data.getBooleanOr("storePrice",this.storePrice),null);
        }
    }

    @Override
    public void appendDisplay(ValueInput data, SettingsDisplayOutput output) {
        if(data.keySet().contains("unlimitedStock") || data.keySet().contains("storePrice")) {
            output.acceptEntry(VALUE_UNLIMITED_STOCK,data.getBooleanOr("unlimitedStock",false));
            output.acceptEntry(VALUE_STORE_PRICE,data.getBooleanOr("storePrice",false));
        }
    }

}
