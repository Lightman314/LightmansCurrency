package io.github.lightman314.lightmanscurrency.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;

import javax.annotation.Nonnull;

public class PlayerEntry {

    public final PlayerReference player;
    public final int color;
    private PlayerEntry(@Nonnull PlayerReference player, int color) { this.player = player; this.color = color; }

    public static PlayerEntry of(@Nonnull PlayerReference player) { return of(player,0xFFFFFF); }
    public static PlayerEntry of(@Nonnull PlayerReference player, int color) { return new PlayerEntry(player,color); }

}
