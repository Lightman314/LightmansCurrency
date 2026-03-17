package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.List;

public class MoneySlot extends EasyMultiBGSlot {

    private static List<Pair<ResourceLocation,ResourceLocation>> backgrounds = null;

    public MoneySlot(MoneyInventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

    @Override
    protected List<Pair<ResourceLocation, ResourceLocation>> getPossibleNoItemIcons() {
        if(backgrounds == null)
        {
            List<Pair<ResourceLocation,ResourceLocation>> temp = new ArrayList<>();
            for(CurrencyType<?> type : MoneyAPI.getApi().AllCurrencyTypes())
                type.addMoneySlotBackground(temp::add, rl -> temp.add(Pair.of(InventoryMenu.BLOCK_ATLAS,rl)));
            backgrounds = ImmutableList.copyOf(temp);
        }
        return backgrounds;
    }

}
