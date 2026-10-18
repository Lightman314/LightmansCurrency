package io.github.lightman314.lightmanscurrency.features.player_data;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.NotificationTab;
import io.github.lightman314.lightmanscurrency.features.player_data.tabs.PlayerStatsTab;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

public class PlayerDataMenu extends TabbedMenu<PlayerDataMenu,PlayerDataTab> {

    public static final TextEntry TOOLTIP_OPEN = TextEntry.tooltip(LCApi.MODID,"player_data.tooltip");

    public PlayerDataMenu(int containerID,Player player) { this(LCMenuTypes.PLAYER_DATA.get(),containerID,player); }
    protected PlayerDataMenu(@Nullable MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId, player);
    }

    @Override
    protected void addInventorySlots(Inventory inventory) { }

    @Override
    protected void collectTabs(TabBuilder<PlayerDataMenu,PlayerDataTab> builder) {
        builder.addTab(new NotificationTab(this));
        builder.addTab(new PlayerStatsTab(this));
    }

    @Override
    public boolean stillValid(Player player) { return true; }

}