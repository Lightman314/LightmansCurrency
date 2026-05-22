package io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.DummyTradeOfferNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tabs.GachaPriceTab;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.trade.GachaDummyTrade;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GachaNode extends DummyTradeOfferNode<GachaDummyTrade> implements IPersistentNode {

    private static final MapCodec<GachaNode> MAP_CODEC = MoneyValue.CODEC.fieldOf("price")
            .xmap(GachaNode::new,GachaNode::getPrice);
    public static final TraderNodeType<GachaNode> TYPE = TraderNodeType.simple(GachaNode::new,MAP_CODEC);

    private final GachaDummyTrade trade = new GachaDummyTrade();

    private MoneyValue price = MoneyValue.empty();
    public MoneyValue getPrice() { return this.price; }
    public boolean setPrice(@Nullable Player player, MoneyValue price)
    {
        if(this.hasPermission(player,Permissions.EDIT_TRADES) && !this.price.equals(price))
        {
            this.price = price;
            this.setChanged(builder -> builder.setMoneyValue("price",this.price));
            return true;
        }
        return false;
    }

    private GachaNode() {}
    private GachaNode(MoneyValue price) {
        this.price = price;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    protected GachaDummyTrade getTrade() {
        return this.trade;
    }

    @Override
    protected Supplier<LazyPacketType<GachaDummyTrade>> getPacketType() { return ModLazyPackets.GACHA_MACHINE_DUMMY; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setMoneyValue("price",this.price);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("price"))
            this.price = data.getMoneyValue("price");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Price"))
            this.price = MoneyValue.load(tag.getCompound("Price"));
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.add("Price",MoneyValue.CODEC.encodeStart(context.ops(),this.price).getOrThrow());
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Price"))
            this.price = MoneyValue.LENIENT_NON_EMPTY_CODEC.decode(context.ops(),json.get("Price")).getOrThrow(JsonSyntaxException::new).getFirst();
        else
            throw new JsonSyntaxException("Expected a 'Price' entry!");
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        //Don't need to call super here, as the gacha machine uses the storage tab as the default tab
        menu.addTab(new GachaPriceTab(menu));
    }
}
