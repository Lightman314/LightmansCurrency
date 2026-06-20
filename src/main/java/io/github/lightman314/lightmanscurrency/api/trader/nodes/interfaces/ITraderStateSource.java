package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;

import java.util.Optional;

public interface ITraderStateSource {

    Optional<TraderState> getCurrentState();

}