package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.api.helpers.capabilities.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

public final class LCCapabilities {

    private LCCapabilities() {}

    public static final class Money {
        private Money() {}
        public static final BlockCapability<MoneyResourceHandler,Direction> BLOCK = BlockCapability.createSided(LCApi.id("money_handler"),MoneyResourceHandler.class);
        public static final EntityCapability<MoneyResourceHandler,Direction> ENTITY = EntityCapability.createSided(LCApi.id("money_handler"),MoneyResourceHandler.class);
        public static final ItemCapability<MoneyResourceHandler,SidedItemAccess> ITEM = ItemCapability.create(LCApi.id("money_handler"),MoneyResourceHandler.class,SidedItemAccess.class);
    }

}
