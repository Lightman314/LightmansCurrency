package io.github.lightman314.lightmanscurrency.api.misc.menus;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasyMultiBGSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MoneySlot extends EasyMultiBGSlot {

    private final List<Pair<ResourceLocation,ResourceLocation>> backgrounds;

    private final Player player;

    public MoneySlot(@Nonnull Container container, int index, int x, int y, @Nonnull Player player) {
        super(container, index, x, y);
        this.player = player;
        List<Pair<ResourceLocation,ResourceLocation>> temp = new ArrayList<>();
        for(CurrencyType type : MoneyAPI.API.AllCurrencyTypes())
            type.addMoneySlotBackground(temp::add, rl -> temp.add(Pair.of(InventoryMenu.BLOCK_ATLAS,rl)));
        this.backgrounds = ImmutableList.copyOf(temp);
    }

    @Override
    protected List<Pair<ResourceLocation, ResourceLocation>> getPossibleNoItemIcons() { return this.backgrounds; }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if(this.locked)
            return false;
        return MoneyAPI.API.ItemAllowedInMoneySlot(this.player,stack);
    }

}
