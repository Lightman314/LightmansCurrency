package io.github.lightman314.lightmanscurrency.api.capability.money;

import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityMoneyViewer {

    private CapabilityMoneyViewer() {}

    public static final ItemCapability<IMoneyViewer,Void> MONEY_VIEWER_ITEM = ItemCapability.create(VersionUtil.lcResource("money_view_item"),IMoneyViewer.class,Void.class);
    public static final BlockCapability<IMoneyViewer,@Nullable Direction> MONEY_VIEWER_BLOCK = BlockCapability.createSided(VersionUtil.lcResource("money_view_block"),IMoneyViewer.class);
    public static final EntityCapability<IMoneyViewer,Void> MONEY_VIEWER_ENTITY = EntityCapability.create(VersionUtil.lcResource("money_view_entity"),IMoneyViewer.class,Void.class);

}
