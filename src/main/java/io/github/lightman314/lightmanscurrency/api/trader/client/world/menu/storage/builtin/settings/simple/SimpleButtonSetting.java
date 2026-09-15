package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleButtonSetting implements SimpleSettingBuilder {

    private final Supplier<Component> buttonText;
    private final TooltipSource buttonTooltip;
    private final BooleanSupplier canEdit;
    private final Runnable onPress;
    private SimpleButtonSetting(Builder builder) {
        this.buttonText = builder.buttonText;
        this.buttonTooltip = builder.buttonTooltip;
        this.canEdit = builder.canEdit;
        this.onPress = builder.onPress;
    }

    @Override
    public int buildWidgets(IPositionalWidgetHolder holder, int width, int startY) {
        //Button
        holder.addChild(ScreenPosition.of(0,startY),TextButton.builder()
                .ofSize(width,20)
                .withText(this.buttonText)
                .active(this.canEdit)
                .tooltip(this.buttonTooltip)
                .onPress(this.onPress)
                .build());
        return 20;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private Builder() {}

        private Supplier<Component> buttonText = Component::empty;
        private TooltipSource buttonTooltip = TooltipSource.EMPTY;
        private BooleanSupplier canEdit = () -> false;
        private Runnable onPress = () -> {};

        public Builder withText(Component text) { return this.withText(() -> text); }
        public Builder withText(Supplier<Component> text) { this.buttonText = text; return this; }
        public <T extends TraderNode> Builder withText(INodeAccess trader, TraderNodeType<T> node,Function<T,Component> accessor) { return this.withText(() -> trader.getNodeValue(node,accessor,Component.empty())); }

        public Builder tooltip(TooltipSource tooltip) { this.buttonTooltip = tooltip; return this; }

        public Builder canEdit(IPermissionAccess perms, Permission<Boolean> permission) { return this.canEdit(() -> perms.getPermission(permission)); }
        public Builder canEdit(BooleanSupplier canEdit) { this.canEdit = canEdit; return this; }

        public Builder onPress(Runnable onPress) { this.onPress = onPress; return this; }

        public SimpleButtonSetting build() { return new SimpleButtonSetting(this); }

    }

}
