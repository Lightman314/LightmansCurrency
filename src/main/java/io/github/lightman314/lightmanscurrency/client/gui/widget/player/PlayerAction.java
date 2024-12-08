package io.github.lightman314.lightmanscurrency.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class PlayerAction {

    private final Consumer<PlayerReference> action;
    private final Function<PlayerReference,Component> tooltip;
    private final Predicate<PlayerReference> hasPermission;
    private final Function<PlayerReference,IconData> icon;
    private PlayerAction(Builder builder)
    {
        this.action = builder.action;
        this.tooltip = builder.tooltip;
        this.hasPermission = builder.hasPermission;
        this.icon = builder.icon;
    }

    public boolean canTrigger(PlayerReference player) { return this.hasPermission.test(player); }

    public void onTrigger(PlayerReference player) { this.action.accept(player); }

    public Component tooltip(PlayerReference player) { return this.tooltip.apply(player); }

    public IconData buttonIcon(PlayerReference player) { return this.icon.apply(player); }


    public static Builder builder() { return new Builder(); }

    public static Builder easyRemove(Consumer<PlayerReference> remove)
    {
        return builder()
                .icon(IconUtil.ICON_X)
                .tooltip(LCText.BUTTON_REMOVE)
                .action(remove);
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder
    {
        private Builder() {}

        private Consumer<PlayerReference> action = p -> {};
        private Function<PlayerReference,Component> tooltip = p -> EasyText.empty();
        private Predicate<PlayerReference> hasPermission = p -> true;
        private Function<PlayerReference,IconData> icon = p -> IconData.Null();

        public Builder action(Consumer<PlayerReference> action) { this.action = action; return this; }
        public Builder tooltip(Component tooltip) { this.tooltip = p -> tooltip; return this; }
        public Builder tooltip(TextEntry tooltip) { this.tooltip = p -> tooltip.get(); return this; }
        public Builder tooltip(Supplier<Component> tooltip) { this.tooltip = p -> tooltip.get(); return this; }
        public Builder tooltip(Function<PlayerReference,Component> tooltip) { this.tooltip = tooltip; return this; }
        public Builder permission(Predicate<PlayerReference> hasPermission) { this.hasPermission = hasPermission; return this; }
        public Builder icon(IconData icon) { this.icon = p -> icon; return this; }
        public Builder icon(Supplier<IconData> icon) { this.icon = p -> icon.get(); return this; }
        public Builder icon(Function<PlayerReference,IconData> icon) { this.icon = icon; return this; }

        public PlayerAction build() { return new PlayerAction(this); }

    }

}
