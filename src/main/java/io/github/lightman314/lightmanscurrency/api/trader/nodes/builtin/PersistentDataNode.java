package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.SimpleOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import io.github.lightman314.lightmanscurrency.features.commands.LCAdminCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public class PersistentDataNode extends SimpleSyncedNode implements IUnitNode, INetworkController, IAdminSettingProvider, IOwnerSource, IAdminInfoProvider, ISettingsMessageListener,IPermissionBlocker {

    private static final MapCodec<PersistentDataNode> MAP_CODEC = Data.CODEC.optionalFieldOf("data").xmap(
            PersistentDataNode::new,n -> n.persistentKey
    );

    public static final TraderNodeType<PersistentDataNode> TYPE = TraderNodeType.simple(PersistentDataNode::new,MAP_CODEC);

    public static final TextEntry TOOLTIP_SETTINGS = TextEntry.tooltip(LCApi.MODID,"persistent_trader.settings");
    public static final TextEntry GUI_ID = TextEntry.gui(LCApi.MODID,"persistent_trader.id");
    public static final TextEntry GUI_OWNER = TextEntry.gui(LCApi.MODID,"persistent_trader.owner");
    public static final TextEntry GUI_NAME = TextEntry.gui(LCApi.MODID,"persistent_trader.name");
    public static final TextEntry BUTTON_CREATE_COPY = TextEntry.button(LCApi.MODID,"persistent_trader.create_copy");

    private Optional<Data> persistentKey = Optional.empty();
    public boolean isPersistent() { return this.persistentKey.isPresent(); }
    public String getPersistentKey() { return this.persistentKey.map(Data::key).orElse(""); }
    @ApiStatus.Internal
    public void flagAsPersistent(String key,Component owner) {
        if(this.persistentKey.isPresent())
            throw new IllegalStateException("Cannot change the persistence key of a trader that's already been defined!");
        this.persistentKey = Optional.of(new Data(key,owner));
        this.owner = Optional.of(new SimpleOwnerHolder(owner,this));
        this.ifNodePresent(WorldNode.TYPE,node -> node.setState(TraderState.PERSISTENT));
    }
    private Optional<IOwnerHolder> owner = Optional.empty();

    public static final TextEntry COMMAND_PERSISTENT_ID = LCAdminCommand.traderListText(TYPE,"persistent_id");

    private PersistentDataNode() {}
    private PersistentDataNode(Optional<Data> data) {
        this.persistentKey = data;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) { }

    @Override
    public void traderCreatePacket(FancyPacketMap.Mutable builder) {
        if(this.persistentKey.isPresent()) {
            Data data = this.persistentKey.get();
            builder.setString("key",data.key)
                    .setText("owner",data.owner);
        }
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("key") && data.contains("owner")) {
            Data pd = new Data(data.getString("key"),data.getText("owner"));
            this.persistentKey = Optional.of(pd);
            this.owner = Optional.of(new SimpleOwnerHolder(pd.owner,this));
        }
    }

    @Override
    public void modifyInfiniteStock(MutableBoolean currentState) {
        if(this.isPersistent())
            currentState.setTrue();
    }

    @Override
    public void modifyStorePrice(MutableBoolean currentState) {
        if(this.isPersistent())
            currentState.setFalse();
    }

    @Override
    public boolean visibleToNetwork() { return this.isPersistent(); }

    @Override
    public Optional<IOwnerHolder> getValidOwner(Optional<IOwnerHolder> currentOwner) {
        //Only provide the persistent owner if present, otherwise use the owner provided by any other node
        if(this.owner.isPresent())
            return this.owner;
        return currentOwner;
    }

    @Override
    public void displayAdminListData(Builder builder) {
        if(this.persistentKey.isPresent())
            builder.accept(-990000,COMMAND_PERSISTENT_ID.get(this.persistentKey.get().key));
    }

    @Override
    public void handleSettingsChange(Player player,FancyPacketMap message) {
        if(message.contains("makePersistentCopy") && LCApi.isInAdminMode(player) && this.isServer()) {
            FancyPacketMap entry = message.getMap("makePersistentCopy");
            String id = entry.getStringOrNull("id");
            String ownerName = entry.getString("owner");
            String name = entry.getString("name");
            TraderDataCache data = TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
            if(data != null)
                data.createPersistentCopy(this.getTrader(),id,ownerName,name);
        }
    }

    //Block all permissions
    @Override
    public boolean blockPermission(Permission<?> permission) {
        if(this.isPersistent())
            return permission != LCPermissions.OPEN_STORAGE;
        return false;
    }

    private record Data(String key,Component owner) {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("persistent_key").forGetter(Data::key),
                ComponentSerialization.CODEC.fieldOf("owner").forGetter(Data::owner)
        ).apply(builder,Data::new));
    }

}