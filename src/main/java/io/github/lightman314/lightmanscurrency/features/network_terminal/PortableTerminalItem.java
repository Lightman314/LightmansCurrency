package io.github.lightman314.lightmanscurrency.features.network_terminal;

import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.ItemInInventoryValidator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PortableTerminalItem extends Item {

    public PortableTerminalItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(!level.isClientSide())
            player.openMenu(TradingTerminalMenu.createProvider(new ItemInInventoryValidator(this)));
        return InteractionResult.SUCCESS;
    }

}
