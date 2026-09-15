package io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemInInventoryValidator extends MenuValidator {

    public static final StreamCodec<RegistryFriendlyByteBuf,ItemInInventoryValidator> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).map(ItemInInventoryValidator::new,v -> v.item);

    private final Item item;
    public ItemInInventoryValidator(ItemStack item) { this(item.getItem()); }
    public ItemInInventoryValidator(ItemLike item) { this.item = item.asItem(); }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ItemInInventoryValidator> getType() { return STREAM_CODEC; }

    @Override
    public boolean stillValid(Player player) {
        //Check the players inventory
        if(player.getInventory().hasAnyMatching(s -> s.is(this.item)))
            return true;
        //Also allow the held item to be the given item just to avoid unintended consequences if the player wants to move it elsewhere
        if(player.containerMenu.getCarried().is(this.item))
            return true;
        //Last but not least, check the curios equipement slots as we often use this menu validation method there
        return LCCuriosHelper.get().hasItem(player,this.item);
    }

}
