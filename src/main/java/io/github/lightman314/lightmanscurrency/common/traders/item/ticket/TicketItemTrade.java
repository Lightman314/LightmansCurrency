package io.github.lightman314.lightmanscurrency.common.traders.item.ticket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.item.CodecData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeType;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TicketItemTrade extends ItemTradeData {

    public static final ItemTradeType<TicketItemTrade> TYPE = new Type();

    private static final MapCodec<TicketItemTrade> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TicketSaleData.CODEC.fieldOf("sale_data_1").forGetter(t -> t.ticketData1),
            TicketSaleData.CODEC.fieldOf("sale_data_2").forGetter(t -> t.ticketData2),
            itemFields()
    ).and(ruleFields(builder)).apply(builder,TicketItemTrade::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,TicketItemTrade> STREAM_CODEC = StreamHelper.combine(itemStreamFields(),
            TicketSaleData.STREAM_CODEC,t -> t.ticketData1,
            TicketSaleData.STREAM_CODEC,t -> t.ticketData2,
            TicketItemTrade::new);

    private final TicketKioskRestriction restriction = new TicketKioskRestriction(this);

    private final TicketSaleData ticketData1 = new TicketSaleData(this, 0);
    private final TicketSaleData ticketData2 = new TicketSaleData(this, 1);

    public TicketItemTrade(boolean validateRules) {
        super(validateRules);
        super.setRestriction(this.restriction);
    }
    private TicketItemTrade(TicketSaleData data1, TicketSaleData data2, CodecData itemData, MoneyValue price) { this(data1,data2,itemData,price,new HashMap<>()); }
    private TicketItemTrade(TicketSaleData data1, TicketSaleData data2, CodecData itemData, MoneyValue price, Map<TradeRuleType<?>,TradeRule> rules) {
        super(itemData,price,rules);
        this.ticketData1.copyFrom(data1);
        this.ticketData2.copyFrom(data2);
    }

    @Override
    public ItemTradeType<?> getType() { return TYPE; }

    @Override
    public void setRestriction(ItemTradeRestriction restriction) { }
    @Override
    public ItemTradeRestriction getRestriction() { return this.restriction; }

    @Override
    public void setItem(ItemStack itemStack, int index) {
        super.setItem(itemStack, index);
        if(index < 2)
            this.getTicketData(index).onSellItemChanged();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.ticketData1.isValid() && this.ticketData2.isValid();
    }

    @Nullable
    public TicketSaleData getTicketData(int index)
    {
        return switch (index) {
            case 0 -> this.ticketData1;
            case 1 -> this.ticketData2;
            default -> null;
        };
    }

    @Override
    public void saveAdditionalSettings(SavedSettingData.MutableNodeAccess node) {
        this.ticketData1.saveSettings(node);
        this.ticketData2.saveSettings(node);
    }

    @Override
    public void saveAdditionalJsonData(JsonObject json, DataContext<JsonElement> context) {
        if(this.getSellItem(0).isEmpty())
        {
            //If no item in the first slot, place the 2nd ticket recipe data in slot 1 as this is what is done by normal item trades
            if(!this.getSellItem(1).isEmpty() && this.ticketData2.tryGetRecipe() != null)
                json.add("TicketRecipe",TicketSaleData.CODEC.encodeStart(context.ops(),this.ticketData2).getOrThrow());
        }
        else
        {
            if(this.ticketData1.tryGetRecipe() != null)
                json.add("TicketRecipe",TicketSaleData.CODEC.encodeStart(context.ops(),this.ticketData1).getOrThrow());
            if(!this.getSellItem(1).isEmpty() && this.ticketData2.tryGetRecipe() != null)
                json.add("TicketRecipe2",TicketSaleData.CODEC.encodeStart(context.ops(),this.ticketData2).getOrThrow());
        }
    }

    @Override
    public void loadAdditionalJsonData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("TicketRecipe"))
            this.ticketData1.copyFrom(TicketSaleData.CODEC.decode(context.ops(),json.get("TicketRecipe")).getOrThrow(JsonSyntaxException::new).getFirst());
        if(json.has("TicketRecipe2"))
            this.ticketData2.copyFrom(TicketSaleData.CODEC.decode(context.ops(),json.get("TicketRecipe2")).getOrThrow(JsonSyntaxException::new).getFirst());
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadFromNBT(tag, lookup);

        if(tag.contains("TicketData1"))
            this.ticketData1.load(tag.getCompound("TicketData1"));
        else //Update old trade data to match the new ticket kiosk data saving methods
            updateFromOldData(this.ticketData1,0);

        if(tag.contains("TicketData2"))
            this.ticketData2.load(tag.getCompound("TicketData2"));
        else
            updateFromOldData(this.ticketData2,1);
    }

    @Override
    public void loadAdditionalSettings(SavedSettingData.NodeAccess node) {
        this.ticketData1.loadSettings(node);
        this.ticketData2.loadSettings(node);
    }

    private void updateFromOldData(TicketSaleData data, int index)
    {
        ItemStack sellItem = this.getActualItem(index);
        if(TicketItem.isTicket(sellItem))
        {
            TicketGroupData group = TicketGroupData.getForTicket(sellItem);
            if(group != null)
            {
                //Turn the "sell item" back into the master ticket
                ItemStack masterTicket = sellItem.transmuteCopy(group.masterTicket);
                //Setting the item will automatically trigger the "onSaleItemChanged" method to recalculate the recipe id
                this.setItem(masterTicket,index);
                //Try to find the exact recipe for a ticket, just in case it defaults to the pass recipe
                for(RecipeHolder<TicketStationRecipe> recipe : this.ticketData1.getMatchingRecipes())
                {
                    if(recipe.value().assembleWithKiosk(masterTicket,data.getData()).getItem() == sellItem.getItem())
                    {
                        data.recipe = recipe.id();
                        return;
                    }
                }
            }
        }
    }

    private static class Type extends ItemTradeType<TicketItemTrade>
    {
        @Override
        public ItemTradeData create(boolean validateTrades) { return new TicketItemTrade(validateTrades); }
        @Override
        public MapCodec<TicketItemTrade> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TicketItemTrade> streamCodec() { return STREAM_CODEC; }
        @Override
        public ItemTradeData changeType(ItemTradeData other) {
            return new TicketItemTrade(new TicketSaleData(null,0),new TicketSaleData(null,0),other.getCodecData(),other.getCost(),other.getRuleMap());
        }
    }

}
