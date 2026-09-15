package io.github.lightman314.lightmanscurrency.client.features.atm;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.ItemInInventoryValidator;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PortableATMItem extends Item {

    public PortableATMItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide()) {
            player.openMenu(ATMMenu.createProvider(new ItemInInventoryValidator(this)));
        }
        return InteractionResult.SUCCESS;
    }
}
