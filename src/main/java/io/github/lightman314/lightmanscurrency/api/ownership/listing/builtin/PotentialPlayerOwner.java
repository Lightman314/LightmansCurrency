package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.ItemStackHelper;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class PotentialPlayerOwner extends PotentialOwner {

    public static final int PLAYER_PRIORITY = 1000000;

    public final PlayerReference player;
    private IconData icon = null;
    public PotentialPlayerOwner(@Nonnull PlayerReference player) { super(PlayerOwner.of(player), PLAYER_PRIORITY); this.player = player; }

    @Nonnull
    @Override
    public IconData getIcon() {
        if(this.icon == null)
            this.icon = IconData.of(ItemStackHelper.skullForPlayer(this.player.getName(this.isClient())));
        return this.icon;
    }

    @Override
    public void appendTooltip(@Nonnull List<Component> tooltip) { LCText.TOOLTIP_OWNER_PLAYER.tooltip(tooltip, this.getName()); }

}
