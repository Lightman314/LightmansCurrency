package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.impl.CoinAPIImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CoinManagementMenu extends LazyMessageMenu {


    public CoinManagementMenu(int id, Inventory inventory) { super(ModMenus.COIN_MANAGEMENT.get(), id, inventory); }

    @Override
    public void HandleMessage(LazyPacketData message) {
        //Server should not be sending any messages to the client. Editing is 100% client-side.
        if(this.isClient())
            return;
        if(message.contains("SaveData") && this.player.hasPermissions(2))
        {
            if(this.player.hasPermissions(2))
            {
                String data = message.getString("SaveData");
                CoinAPIImpl.LoadEditedData(data);
            }
            else
                LightmansCurrency.LogError("Non-admin player " + this.player.getDisplayName().getString() + " attempted to edit the coin data!");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    protected void onValidationTick(Player player) {
        //Close the menu if the player lost their admin priveledges.
        if(!player.hasPermissions(2))
            this.player.closeContainer();
    }

}
