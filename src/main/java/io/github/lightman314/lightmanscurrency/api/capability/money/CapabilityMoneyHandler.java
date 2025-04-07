package io.github.lightman314.lightmanscurrency.api.capability.money;

import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityMoneyHandler {

    /**
     * Important Note:<br>
     * If an item-related Money Handler required knowledge of which logical side it's being interacted on, please implement {@link io.github.lightman314.lightmanscurrency.api.misc.ISidedObject ISidedObject} so that it can be properly flagged<br>
     * All items with a Money Handler capability will automatically function as trader payment, etc.
     */
    public static final ItemCapability<IMoneyHandler,Void> MONEY_HANDLER_ITEM = ItemCapability.createVoid(VersionUtil.lcResource("money_handler_item"),IMoneyHandler.class);
    public static final BlockCapability<IMoneyHandler,@Nullable Direction> MONEY_HANDLER_BLOCK = BlockCapability.createSided(VersionUtil.lcResource("money_handler_block"),IMoneyHandler.class);
    public static final EntityCapability<IMoneyHandler,Void> MONEY_HANDLER_ENTITY = EntityCapability.createVoid(VersionUtil.lcResource("money_handler_entity"),IMoneyHandler.class);

}
