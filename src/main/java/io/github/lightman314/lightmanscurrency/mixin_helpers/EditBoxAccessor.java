package io.github.lightman314.lightmanscurrency.mixin_helpers;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;

import javax.annotation.Nullable;
import java.util.Optional;

public interface EditBoxAccessor {

    default void lightmanscurrencySetAlreadyRenderered(boolean alreadyRendered) {}
    default Optional<ScreenArea> lightmanscurrencyGetScissorArea() { return Optional.empty(); }
    default void lightmanscurrencySetScissorArea(@Nullable ScreenArea scissorArea) {}

}