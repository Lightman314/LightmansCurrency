package io.github.lightman314.lightmanscurrency.api.upgrades;

import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;

public record UpgradeReference(UpgradeType type,DataComponentGetter data) implements TypedInstance<UpgradeType> {

    @Override
    public Holder<UpgradeType> typeHolder() { return this.type.typeHolder(); }

}