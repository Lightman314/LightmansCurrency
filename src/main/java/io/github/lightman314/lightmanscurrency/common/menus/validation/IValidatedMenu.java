package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;

import javax.annotation.Nonnull;

public interface IValidatedMenu {
    @Nonnull
    MenuValidator getValidator();
}