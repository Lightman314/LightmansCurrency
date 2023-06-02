package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public abstract class CoinChestUpgrade extends UpgradeType {

    public boolean allowsDuplicates() { return false; }

    public abstract void HandleMenuMessage(CoinChestMenu menu, CoinChestUpgradeData data, LazyPacketData message);

    public void OnStorageChanged(CoinChestBlockEntity be, CoinChestUpgradeData data) {}
    public void OnEquip(CoinChestBlockEntity be, CoinChestUpgradeData data) {}
    public boolean BlockAccess(CoinChestBlockEntity be, CoinChestUpgradeData data, Player player) { return false; }
    public void OnValidBlockRemoval(CoinChestBlockEntity be, CoinChestUpgradeData data) { }
    public void OnBlockRemoval(CoinChestBlockEntity be, CoinChestUpgradeData data) { }
    
    public abstract void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer);

    @Override
    protected List<String> getDataTags() { return ImmutableList.of(); }

    @Override
    protected Object defaultTagValue(String tag) { return null; }

}
