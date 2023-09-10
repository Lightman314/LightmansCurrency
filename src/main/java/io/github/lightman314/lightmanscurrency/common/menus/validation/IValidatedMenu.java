package io.github.lightman314.lightmanscurrency.common.menus.validation;

import javax.annotation.Nonnull;

public interface IValidatedMenu {
    @Nonnull
    MenuValidator getValidator();
}
