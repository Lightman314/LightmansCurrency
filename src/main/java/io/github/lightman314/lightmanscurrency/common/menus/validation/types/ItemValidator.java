package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemValidator extends MenuValidator {

    public static MenuValidatorType TYPE = new Type();

    private final Item item;
    public ItemValidator(ItemLike item) {
        super(TYPE);
        this.item = item.asItem();
    }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) { buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(this.item)); }

    @Override
    protected void saveAdditional(CompoundTag tag) { tag.putString("Item",BuiltInRegistries.ITEM.getKey(this.item).toString()); }

    @Override
    public boolean stillValid(Player player) {
        int count = InventoryUtil.GetItemCount(player.getInventory(),this.item);
        if(count > 0)
            return true;
        if(LCCurios.isLoaded() && LCCurios.hasItem(player,s -> s.is(this.item)))
            return true;
        return false;
    }


    private static class Type extends MenuValidatorType
    {

        protected Type() { super(VersionUtil.lcResource("item")); }

        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return new ItemValidator(BuiltInRegistries.ITEM.get(buffer.readResourceLocation())); }
        @Override
        public MenuValidator load(CompoundTag tag) { return new ItemValidator(BuiltInRegistries.ITEM.get(VersionUtil.parseResource(tag.getString("Item")))); }
    }

}
