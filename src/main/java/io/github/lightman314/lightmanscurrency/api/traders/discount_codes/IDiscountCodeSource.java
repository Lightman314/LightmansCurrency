package io.github.lightman314.lightmanscurrency.api.traders.discount_codes;

import java.util.Set;

public interface IDiscountCodeSource {

    default int priority() { return 0; }
    boolean containsCode(String code);
    Set<Integer> getDiscountCodes();
    boolean consumeCode(String code);

}
