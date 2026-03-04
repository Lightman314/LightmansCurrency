package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class CoinChestUpgrade extends UpgradeType {

    public boolean alwayActive() { return false; }

    public abstract void HandleMenuMessage(CoinChestMenu menu, CoinChestUpgradeData data, LazyPacketData message);

    public void OnStorageChanged(CoinChestBlockEntity be, CoinChestUpgradeData data) {}
    public void OnEquip(CoinChestBlockEntity be, CoinChestUpgradeData data) {}
    public boolean BlockAccess(CoinChestBlockEntity be, CoinChestUpgradeData data, @Nullable Player player) { return false; }
    public void OnValidBlockRemoval(CoinChestBlockEntity be, CoinChestUpgradeData data) { }
    public void OnBlockRemoval(CoinChestBlockEntity be, CoinChestUpgradeData data) { }
    
    public abstract void addClientTabs(CoinChestUpgradeData data, Object screen, Consumer<Object> consumer);

    public boolean isActive(CoinChestUpgradeData data) { return this.alwayActive() || data.getData(ModDataComponents.UPGRADE_ACTIVE,true); }
    public void setActive(CoinChestUpgradeData data, boolean active) { data.setData(ModDataComponents.UPGRADE_ACTIVE,active); }

    @Override
    protected List<Component> getBuiltInTargets() { return ImmutableList.of(formatTarget(ModBlocks.COIN_CHEST)); }

}
