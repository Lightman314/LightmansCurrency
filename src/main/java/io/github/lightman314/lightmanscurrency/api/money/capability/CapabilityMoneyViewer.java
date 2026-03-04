package io.github.lightman314.lightmanscurrency.api.money.capability;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityMoneyViewer {

    private CapabilityMoneyViewer() {}

    public static final ItemCapability<IMoneyViewer,Void> MONEY_VIEWER_ITEM = ItemCapability.create(LightmansCurrency.id("money_view_item"),IMoneyViewer.class,Void.class);
    public static final BlockCapability<IMoneyViewer,@Nullable Direction> MONEY_VIEWER_BLOCK = BlockCapability.createSided(LightmansCurrency.id("money_view_block"),IMoneyViewer.class);
    public static final EntityCapability<IMoneyViewer,Void> MONEY_VIEWER_ENTITY = EntityCapability.create(LightmansCurrency.id("money_view_entity"),IMoneyViewer.class,Void.class);

}
