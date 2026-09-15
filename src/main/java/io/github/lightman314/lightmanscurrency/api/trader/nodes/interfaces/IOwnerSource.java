package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;

import java.util.Optional;

public interface IOwnerSource {

    Optional<IOwnerHolder> getValidOwner();

}