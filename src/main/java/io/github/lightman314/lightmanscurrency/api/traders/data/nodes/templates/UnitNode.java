package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

/**
 * A special node that will *exist* on the trader that it's added to, but has no data that needs to be written to file.<br>
 * @see TraderNodeType#unit(Supplier) for an easy type constructor
 */
public abstract class UnitNode extends TraderNode {
    @Override
    public final void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { }
}
