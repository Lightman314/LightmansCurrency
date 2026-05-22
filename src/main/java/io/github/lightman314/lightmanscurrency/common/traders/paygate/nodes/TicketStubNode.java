package io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IContentProvider;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tickets.ITicketRelevanceSource;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class TicketStubNode extends SyncedTraderNode implements IContentProvider {

    private static final MapCodec<TicketStubNode> MAP_CODEC = CodecHelper.UNLIMITED_ITEM.listOf()
            .fieldOf("stubs").xmap(TicketStubNode::new,TicketStubNode::getTicketStubStorage);

    public static final TraderNodeType<TicketStubNode> TYPE = TraderNodeType.simple(TicketStubNode::new,MAP_CODEC);

    private final List<ItemStack> storedTicketStubs = new ArrayList<>();
    public List<ItemStack> getTicketStubStorage() { return this.storedTicketStubs; }

    private TicketStubNode() {}
    private TicketStubNode(List<ItemStack> ticketStubs) { this.storedTicketStubs.addAll(ticketStubs); }

    public int getStoredTicketStubs() {
        int count = 0;
        for(ItemStack stack : this.storedTicketStubs)
            count += stack.getCount();
        return count;
    }
    public void addTicketStub(ItemStack stub)
    {
        //Don't bother storing the ticket stubs if creative.
        if(!this.trader.shouldStorePurchases())
            return;
        for(ItemStack s : this.storedTicketStubs)
        {
            if(stub.getItem() == s.getItem())
            {
                s.grow(stub.getCount());
                stub.setCount(0);
                break;
            }
        }
        if(!stub.isEmpty())
            this.storedTicketStubs.add(stub.copyAndClear());
        this.setChanged(builder -> builder.setList("stubs",this.storedTicketStubs,ModLazyPackets.ITEM_STACK));
    }
    public void collectTicketStubs(Player player)
    {
        if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
            return;
        for(ItemStack stub : this.storedTicketStubs)
            ItemHandlerHelper.giveItemToPlayer(player, stub);
        this.storedTicketStubs.clear();
        this.setChanged(builder -> builder.setList("stubs",this.storedTicketStubs,ModLazyPackets.ITEM_STACK));
    }

    public boolean areTicketStubsRelevant() {
        if(this.storedTicketStubs.isEmpty())
        {
            for(TraderNode node : this.trader.getNodeIterable())
            {
                if(node instanceof ITicketRelevanceSource source && source.areTicketsRelevant())
                    return true;
            }
            return false;
        }
        return true;
        //return this.trades.stream().anyMatch(t -> t.isTicketTrade() && t.shouldStoreTicketStubs());
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setList("stubs",this.storedTicketStubs,ModLazyPackets.ITEM_STACK);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("stubs"))
        {
            this.storedTicketStubs.clear();
            this.storedTicketStubs.addAll(data.getList("stubs",ModLazyPackets.ITEM_STACK));
        }
    }

    @Override
    @Deprecated
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Load Ticket Stubs
        if(tag.contains("TicketStubs"))
        {
            int count = tag.getInt("TicketStubs");
            this.storedTicketStubs.clear();
            if(count > 0)
                this.storedTicketStubs.add(new ItemStack(ModItems.TICKET_STUB.get(), count));
        }
        else if(tag.contains("Stubs"))
        {
            ListTag list = tag.getList("Stubs", Tag.TAG_COMPOUND);
            this.storedTicketStubs.clear();
            for(int i = 0; i < list.size(); ++i)
            {
                ItemStack stack = OldDataHelper.loadItem(list.getCompound(i),lookup);
                if(!stack.isEmpty())
                    this.storedTicketStubs.add(stack);
            }
        }
    }

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> splitList = new ArrayList<>();
        for(ItemStack stub : this.storedTicketStubs)
        {
            ItemStack stack = stub.copy();
            while(!stack.isEmpty())
                splitList.add(stack.split(Math.min(stack.getMaxStackSize(),stack.getCount())));
        }
        return splitList;
    }

}
