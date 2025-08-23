package io.github.lightman314.lightmanscurrency.api.traders.discount_codes;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IDiscountCodeSource {

    default int priority() { return 0; }
    boolean containsCode(String code);
    Set<Integer> getDiscountCodes();
    boolean consumeCode(String code);

}
