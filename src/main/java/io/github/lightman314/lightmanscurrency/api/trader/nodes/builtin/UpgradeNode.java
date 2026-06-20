package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class UpgradeNode extends SimpleSyncedNode implements IUpgradeable {

    private static final MapCodec<UpgradeNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("storage").forGetter(n -> n.storage.getContents())
    ).apply(builder,UpgradeNode::new));
    public static final TraderNodeType<UpgradeNode> TYPE = TraderNodeType.simple(UpgradeNode::new,MAP_CODEC);

    private final UpgradeStorage storage = new UpgradeStorage(5,this);

    private UpgradeNode() { this.storage.setListener(this::setUpgradesChanged); }
    private UpgradeNode(List<ItemStack> contents)
    {
        this();
        this.storage.copyFrom(contents);
    }

    @Override
    public void updateArgument(Optional<Object> argument) {
        if(argument.isPresent() && argument.get() instanceof Number num)
            this.storage.overrideSize(num.intValue());
    }

    private void setUpgradesChanged(List<ItemStack> oldState, List<ItemStack> newState) {
        this.setChanged(builder -> builder.modifyMap("storage_update",this.storage.createPacket(oldState,newState)));
        for(IUpgradeListener listener : this.trader.getNodes(IUpgradeListener.class))
            listener.afterUpgradesChanged(this.storage);
    }

    @Override
    public TraderNodeType<?> getType() {
        return TYPE;
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setList("storage",LCFancyPacketTypes.ITEM_STACK,this.storage.getContents());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage",LCFancyPacketTypes.ITEM_STACK));
        if(data.contains("storage_update"))
            this.storage.processPacket(data.getMap("storage_update"));
    }

    @Override
    public UpgradeStorage getStorage() { return this.storage; }

    @Override
    public boolean allowUpgrade(UpgradeType type) {
        for(IUpgradeUser node : this.trader.getNodes(IUpgradeUser.class))
        {
            if(node.allowUpgrade(type))
                return true;
        }
        return false;
    }

}
