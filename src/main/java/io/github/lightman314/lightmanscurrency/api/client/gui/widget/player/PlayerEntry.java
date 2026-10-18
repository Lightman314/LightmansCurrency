package io.github.lightman314.lightmanscurrency.api.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.world.item.ItemStack;

public record PlayerEntry(PlayerReference player,int color) {
    public PlayerEntry(PlayerReference player) { this(player,-1); }
    public String getName() { return this.player.getName(ISidedContext.LOGICAL_CLIENT); }
    public ItemStack getSkull() { return this.player.getSkull().create(); }
}