package io.github.lightman314.lightmanscurrency.api.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PlayerAction {

    private final Consumer<PlayerReference> action;
    private final Function<PlayerReference,Component> tooltip;
    private final Predicate<PlayerReference> hasPermission;
    private final Function<PlayerReference,IconData> icon;
    private PlayerAction(Builder builder) {
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
    public static Builder easyRemove(Consumer<PlayerReference> remove) {
        return builder()
                .icon(SpriteIcon.of(LCApi.id("icon/sign_x")))
                .tooltip(PlayerReference.TOOLTIP_REMOVE_PLAYER)
                .action(remove);
    }

    public static final class Builder {
        private Builder() {}

        private Consumer<PlayerReference> action = p -> {};
        private Function<PlayerReference,Component> tooltip = p -> Component.empty();
        private Predicate<PlayerReference> hasPermission = p -> true;
        private Function<PlayerReference,IconData> icon = p -> IconData.empty();

        public Builder action(Consumer<PlayerReference> action) { this.action = action; return this; }

        public Builder tooltip(Component tooltip) { return this.tooltip(p -> tooltip); }
        public Builder tooltip(TextEntry tooltip) { return this.tooltip(p -> tooltip.get()); }
        public Builder tooltip(Supplier<Component> tooltip) { return this.tooltip(p -> tooltip.get()); }
        public Builder tooltip(Function<PlayerReference,Component> tooltip) { this.tooltip = tooltip; return this; }

        public Builder permission(Predicate<PlayerReference> hasPermission) { this.hasPermission = hasPermission; return this; }

        public Builder icon(IconData icon) { return this.icon(p -> icon); }
        public Builder icon(Supplier<IconData> icon) { return this.icon(p -> icon.get()); }
        public Builder icon(Function<PlayerReference,IconData> icon) { this.icon = icon; return this; }

        public PlayerAction build() { return new PlayerAction(this); }

    }

}