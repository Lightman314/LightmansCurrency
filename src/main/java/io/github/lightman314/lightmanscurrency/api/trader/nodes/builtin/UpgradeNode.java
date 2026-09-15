package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITraderDestructionListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class UpgradeNode extends SimpleSyncedNode implements IUpgradeable, ITraderDestructionListener {

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
    public void updateArgument(TraderArguments arguments) {
        Optional<Number> arg = arguments.tryGet(TYPE,Number.class);
        if(arg.isPresent())
            this.storage.overrideSize(arg.get().intValue());
    }

    private void setUpgradesChanged(Consumer<FancyPacketMap.Mutable> packetBuilder) {
        this.setChanged(builder -> builder.modifyMap("storage_update",packetBuilder));
        for(IUpgradeListener listener : this.getNodes(IUpgradeListener.class))
            listener.afterUpgradesChanged(this.storage);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void traderCreatePacket(FancyPacketMap.Mutable builder) {
        builder.setInt("upgrade_count",this.storage.size());
    }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setList("storage",LCFancyPacketTypes.ITEM_STACK,this.storage.getContents());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("upgrade_count"))
            this.storage.overrideSize(data.getInt("upgrade_count"));
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage",LCFancyPacketTypes.ITEM_STACK));
        if(data.contains("storage_update"))
            this.storage.processPacket(data.getMap("storage_update"));
    }

    @Override
    public UpgradeStorage getStorage() { return this.storage; }

    @Override
    public boolean allowUpgrade(UpgradeType type) {
        for(IUpgradeUser node : this.getNodes(IUpgradeUser.class))
        {
            if(node.allowUpgrade(type))
                return true;
        }
        return false;
    }

    @Override
    public void onTraderDestroyed(IOwnerHolder owner, Consumer<ItemStack> itemSpawner, Optional<MoneyResourceHandler> playerMoney) {
        List<ItemStack> drops = new ArrayList<>();
        for(ItemStack s : this.storage.getContents()) {
            if(!s.isEmpty())
                drops.add(s);
        }
        //Combine the upgrades into larger stacks if relevant
        ItemHelper.combineStacks(drops).forEach(itemSpawner);
    }

}
