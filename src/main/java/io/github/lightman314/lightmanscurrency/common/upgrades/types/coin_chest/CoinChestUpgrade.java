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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public abstract class CoinChestUpgrade extends UpgradeType {

    public boolean alwayActive() { return false; }

    public abstract void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message);

    public void OnStorageChanged(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {}
    public void OnEquip(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {}
    public boolean BlockAccess(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data, @Nonnull Player player) { return false; }
    public void OnValidBlockRemoval(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) { }
    public void OnBlockRemoval(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) { }
    
    public abstract void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer);

    public boolean isActive(@Nonnull CoinChestUpgradeData data) { return this.alwayActive() || data.getData(ModDataComponents.UPGRADE_ACTIVE,true); }
    public void setActive(@Nonnull CoinChestUpgradeData data, boolean active) { data.setData(ModDataComponents.UPGRADE_ACTIVE,active); }

    @Nonnull
    @Override
    protected List<Component> getBuiltInTargets() { return ImmutableList.of(formatTarget(ModBlocks.COIN_CHEST)); }

}
