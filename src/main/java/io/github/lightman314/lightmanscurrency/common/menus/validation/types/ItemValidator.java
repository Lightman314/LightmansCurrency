package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

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
        //Check their items
        int count = InventoryUtil.GetItemCount(player.getInventory(),s -> s.is(this.item));
        if(count > 0)
            return true;
        //If not in their items, check curios
        return LCCurios.hasItem(player, s -> s.is(this.item));
    }


    private static class Type extends MenuValidatorType
    {

        protected Type() { super(LightmansCurrency.id("item")); }

        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return new ItemValidator(BuiltInRegistries.ITEM.get(buffer.readResourceLocation())); }
        @Override
        public MenuValidator load(CompoundTag tag) { return new ItemValidator(BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("Item")))); }
    }

}
