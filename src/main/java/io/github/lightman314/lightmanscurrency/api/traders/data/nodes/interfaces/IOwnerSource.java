package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;

import javax.annotation.Nullable;

public interface IOwnerSource {

    @Nullable
    OwnerData getValidOwner();

}
