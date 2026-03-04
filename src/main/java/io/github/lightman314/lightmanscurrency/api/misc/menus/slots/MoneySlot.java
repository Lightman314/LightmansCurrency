package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MoneySlot extends EasyMultiBGSlot {

    private final List<Pair<ResourceLocation,ResourceLocation>> backgrounds;

    public MoneySlot(MoneyInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        List<Pair<ResourceLocation,ResourceLocation>> temp = new ArrayList<>();
        for(CurrencyType<?> type : MoneyAPI.getApi().AllCurrencyTypes())
            type.addMoneySlotBackground(temp::add, rl -> temp.add(Pair.of(InventoryMenu.BLOCK_ATLAS,rl)));
        this.backgrounds = ImmutableList.copyOf(temp);
    }

    @Override
    protected List<Pair<ResourceLocation, ResourceLocation>> getPossibleNoItemIcons() { return this.backgrounds; }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(this.locked)
            return false;
        return super.mayPlace(stack);
    }

}
