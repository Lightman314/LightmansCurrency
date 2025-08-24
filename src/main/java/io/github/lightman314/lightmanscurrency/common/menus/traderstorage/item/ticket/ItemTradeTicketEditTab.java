package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ticket;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ticket.ItemTradeTicketEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTradeTicketEditTab extends ItemTradeEditTab {

    public ItemTradeTicketEditTab(ITraderStorageMenu menu) { super(menu); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object createClientTab(Object screen) { return new ItemTradeTicketEditClientTab(screen,this);}

    @Override
    public TicketItemTrade getTrade() {
        if(super.getTrade() instanceof TicketItemTrade tt)
            return tt;
        return null;
    }

    public void ChangeRecipe(@Nullable ResourceLocation recipe, int index)
    {
        TicketItemTrade trade = this.getTrade();
        if(trade != null)
        {
            TicketItemTrade.TicketSaleData data = trade.getTicketData(index);
            if(data == null)
                return;
            data.setRecipe(recipe);
            this.menu.getTrader().markTradesDirty();
            if(this.isClient())
            {
                if(recipe == null)
                {
                    this.menu.SendMessage(this.builder()
                            .setInt("Slot",index)
                            .setFlag("DeleteRecipe"));
                }
                else
                {
                    this.menu.SendMessage(this.builder()
                            .setInt("Slot",index)
                            .setString("ChangeRecipe",recipe.toString()));
                }
            }
        }
    }

    public void ChangeCode(String code, int index)
    {
        TicketItemTrade trade = this.getTrade();
        if(trade != null)
        {
            TicketItemTrade.TicketSaleData data = trade.getTicketData(index);
            if(data == null)
                return;
            data.setCode(code);
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("Slot",index)
                        .setString("ChangeCode",code));
            }
        }
    }

    public void ChangeDurability(int durability, int index)
    {
        TicketItemTrade trade = this.getTrade();
        if(trade != null)
        {
            TicketItemTrade.TicketSaleData data = trade.getTicketData(index);
            if(data == null)
                return;
            data.setDurability(durability);
            if(this.isClient())
            {
                this.menu.SendMessage(this.builder()
                        .setInt("Slot",index)
                        .setInt("ChangeDurability",durability));
            }
        }
    }

    @Override
    public void receiveMessage(LazyPacketData message) {
        super.receiveMessage(message);
        if(message.contains("Slot"))
        {
            int slot = message.getInt("Slot");
            if(message.contains("ChangeRecipe"))
                this.ChangeRecipe(VersionUtil.parseResource(message.getString("ChangeRecipe")),slot);
            if(message.contains("DeleteRecipe"))
                this.ChangeRecipe(null,slot);
            if(message.contains("ChangeCode"))
                this.ChangeCode(message.getString("ChangeCode"),slot);
            if(message.contains("ChangeDurability"))
                this.ChangeDurability(message.getInt("ChangeDurability"),slot);
        }

    }
}