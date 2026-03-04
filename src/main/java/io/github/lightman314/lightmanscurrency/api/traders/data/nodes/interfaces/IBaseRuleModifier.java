package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IBaseRuleModifier {

    default void showOwnerInTitle(AtomicBoolean result) {}
    default void hasInfiniteStock(AtomicBoolean result) {}
    default void shouldStoreMoney(AtomicBoolean result) {}

}
