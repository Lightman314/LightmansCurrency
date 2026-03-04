package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IOwnerSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class FakeOwnerNode extends SyncedTraderNode implements IOwnerSource {

    private static final MapCodec<FakeOwnerNode> MAP_CODEC = ComponentSerialization.CODEC.fieldOf("owner").xmap(FakeOwnerNode::new,n -> n.owner);

    public static final TraderNodeType<FakeOwnerNode> TYPE = TraderNodeType.advanced(FakeOwnerNode::factory,MAP_CODEC);

    private Component owner;
    private final OwnerData data;

    private FakeOwnerNode(Component owner) {
        this.owner = owner;
        this.data = new OwnerData(this);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }
    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setText("owner",this.owner);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("owner"))
            this.owner = data.getText("owner");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { }

    @Override
    public OwnerData getValidOwner() {
        //Update the "fake owner"
        this.data.SetOwner(FakeOwner.of(this.owner));
        return this.data;
    }

    private static FakeOwnerNode factory(@Nullable Object argument)
    {
        if(argument instanceof Component owner)
            return new FakeOwnerNode(owner);
        if(argument instanceof String s)
            return new FakeOwnerNode(EasyText.literal(s));
        if(argument != null)
            return new FakeOwnerNode(EasyText.literal(argument.toString()));
        return new FakeOwnerNode(Component.empty());
    }

}
