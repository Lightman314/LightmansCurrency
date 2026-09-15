package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PotentialPlayerOwner extends PotentialOwner {

    public static final int PLAYER_PRIORITY = 1000000;

    public final PlayerReference player;
    private IconData icon = null;
    public PotentialPlayerOwner(PlayerReference player) { super(PlayerOwner.of(player), PLAYER_PRIORITY); this.player = player; }

    @Override
    public IconData getIcon() {
        if(this.icon == null)
            this.icon = ItemIcon.of(this.player.getSkull());
        return this.icon;
    }

    @Override
    public void appendTooltip(List<Component> tooltip) { PlayerOwner.TOOLTIP_OWNER_PLAYER.tooltip(tooltip,this.getName()); }

}