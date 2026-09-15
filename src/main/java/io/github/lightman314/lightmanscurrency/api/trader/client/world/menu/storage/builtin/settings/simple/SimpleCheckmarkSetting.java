package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SimpleCheckmarkSetting implements SimpleSettingBuilder {

    private final BooleanSupplier currentValue;
    private final BooleanSupplier canEdit;
    private final Runnable onPress;
    private final Supplier<Component> label;
    private SimpleCheckmarkSetting(Builder builder) {
        this.currentValue = builder.currentValue;
        this.canEdit = builder.canEdit;
        this.onPress = builder.onPress.apply(this);
        this.label = builder.label;
    }

    @Override
    public int buildWidgets(IPositionalWidgetHolder holder, int width, int startY) {
        //Check Box
        holder.addChild(ScreenPosition.of(0,startY),SpriteButton.builder()
                .withSprite(LCSprites.CHECKBOX.buildSprite(this.currentValue))
                .active(this.canEdit)
                .onPress(this.onPress)
                .build());
        //Label
        holder.addChild(ScreenPosition.of(12,startY + 1),TextDisplayWidget.builder()
                .withText(this.label)
                .ofWidth(width - 14)
                .build());
        return 10;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private Builder() {}

        private BooleanSupplier currentValue = () -> false;
        private BooleanSupplier canEdit = () -> false;
        private Function<SimpleCheckmarkSetting,Runnable> onPress = s -> () -> {};
        private Supplier<Component> label = Component::empty;

        public <T extends TraderNode> Builder withCurrentValue(INodeAccess trader,TraderNodeType<T> node,Function<T,Boolean> accessor) { return this.withCurrentValue(() -> trader.getNodeValue(node,accessor,false)); }
        public Builder withCurrentValue(BooleanSupplier currentValue) { this.currentValue = currentValue; return this; }

        public Builder canEdit(IPermissionAccess perms, Permission<Boolean> permission) { return this.canEdit(() -> perms.getPermission(permission)); }
        public Builder canEdit(BooleanSupplier canEdit) { this.canEdit = canEdit; return this; }

        public Builder onPress(BiConsumer<TraderNodeType<?>,FancyPacketMap> sender,TraderNodeType<?> node,String key) {
            this.onPress = s -> () -> sender.accept(node,FancyPacketMap.map().setBoolean(key,!s.currentValue.getAsBoolean()));
            return this;
        }
        public Builder onPress(Runnable onPress) { this.onPress = c -> onPress; return this; }

        public Builder withLabel(Component label) { return this.withLabel(() -> label); }
        public Builder withLabel(TextEntry label) { return this.withLabel(label::get); }
        public Builder withLabel(Supplier<Component> label) { this.label = label; return this; }

        public SimpleCheckmarkSetting build() { return new SimpleCheckmarkSetting(this); }

    }

}
