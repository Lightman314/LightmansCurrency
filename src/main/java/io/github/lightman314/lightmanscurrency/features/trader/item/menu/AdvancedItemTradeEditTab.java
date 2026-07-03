package io.github.lightman314.lightmanscurrency.features.trader.item.menu;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.AdvancedTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class AdvancedItemTradeEditTab extends AdvancedTradeEditTab {

    public static final Identifier KEY = LCApi.id("advanced_item_trade_edit");

    public AdvancedItemTradeEditTab(TraderStorageMenu menu) { super(menu); }

    @Override
    public Identifier getKey() { return KEY; }

    public void setItem(int slot,ItemStack stack) {
        if(this.getPermission(BuiltInPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof ItemTradeData trade)
        {
            TradeItem item = trade.getItem(slot);
            item.setStack(stack);
            trade.setItemChanged(slot);
            this.send(FancyPacketMap.newMutable()
                    .setMap("itemEdit",FancyPacketMap.newMutable()
                        .setInt("slot",slot)
                        .setItem("item",stack)));
        }
    }

    public void setCustomName(int slot,String customName) {
        if(this.getPermission(BuiltInPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof ItemTradeData trade)
        {
            TradeItem item = trade.getItem(slot);
            item.setNameChange(customName);
            trade.setItemChanged(slot);
            this.send(FancyPacketMap.newMutable()
                    .setMap("customName",FancyPacketMap.newMutable()
                        .setInt("slot",slot)
                        .setString("name",customName)));
        }
    }

    public void setStrict(int slot,boolean strict) {
        if(this.getPermission(BuiltInPermissions.EDIT_TRADES) && this.getSelectedTrade() instanceof ItemTradeData trade)
        {
            TradeItem item = trade.getItem(slot);
            item.setStrict(strict);
            trade.setItemChanged(slot);
            this.send(FancyPacketMap.newMutable()
                    .setMap("strictItem",FancyPacketMap.newMutable()
                            .setInt("slot",slot)
                            .setBoolean("strict",strict)));
        }
    }

    @Override
    public void handleMessage(FancyPacketMap packet) {
        super.handleMessage(packet);
        if(packet.contains("itemEdit"))
        {
            FancyPacketMap edit = packet.getMap("itemEdit");
            this.setItem(edit.getInt("slot"),edit.getItem("item"));
        }
        if(packet.contains("customName"))
        {
            FancyPacketMap edit = packet.getMap("customName");
            this.setCustomName(edit.getInt("slot"),edit.getString("name"));
        }
        if(packet.contains("strictItem"))
        {
            FancyPacketMap edit = packet.getMap("strictItem");
            this.setStrict(edit.getInt("slot"),edit.getBoolean("strict"));
        }
    }
}
