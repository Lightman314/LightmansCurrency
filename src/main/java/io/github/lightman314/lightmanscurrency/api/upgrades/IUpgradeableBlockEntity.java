package io.github.lightman314.lightmanscurrency.api.upgrades;

import javax.annotation.Nullable;

public interface IUpgradeableBlockEntity {

    @Nullable
    default IUpgradeable getUpgradeable() { if(this instanceof IUpgradeable u) return u; return null; }

}