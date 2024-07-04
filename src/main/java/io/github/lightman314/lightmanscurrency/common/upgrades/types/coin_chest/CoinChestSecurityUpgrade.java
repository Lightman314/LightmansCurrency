package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.SecurityUpgradeTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data.SecurityUpgradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoinChestSecurityUpgrade extends CoinChestUpgrade {

    @Override
    public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message) {
        if(message.contains("SetOwner"))
        {
            Owner owner = message.getOwner("SetOwner");
            if(owner != null)
            {
                OwnerData ownerData = this.parseOwnerData(menu.be, data);
                ownerData.SetOwner(owner);
                this.saveOwnerData(menu.be,data, ownerData);
            }
        }
        else if(message.contains("SetPlayerOwner"))
        {
            PlayerReference player = PlayerReference.of(false, message.getString("SetPlayerOwner"));
            if(player != null)
            {
                OwnerData owner = this.parseOwnerData(menu.be, data);
                owner.SetOwner(PlayerOwner.of(player));
                this.saveOwnerData(menu.be,data, owner);
            }
        }
    }

    public boolean isAdmin(CoinChestBlockEntity be, CoinChestUpgradeData data, Player player)
    {
        OwnerData owner = this.parseOwnerData(be, data);
        if(owner.hasOwner())
            return owner.isAdmin(player);
        return true;
    }

    /**
     * Whether the given player has member level access.
     * Will always return true if no owner is defined.
     */
    public boolean isMember(CoinChestBlockEntity be, CoinChestUpgradeData data, Player player)
    {
        OwnerData owner = this.parseOwnerData(be, data);
        if(owner.hasOwner())
            return owner.isMember(player);
        return true;
    }

    @Nonnull
    public OwnerData parseOwnerData(CoinChestBlockEntity be, CoinChestUpgradeData data)
    {
        return data.getData(ModDataComponents.SECURITY_UPGRADE_DATA,SecurityUpgradeData.DEFAULT).parseData(be);
    }

    public void saveOwnerData(@Nonnull CoinChestBlockEntity be, CoinChestUpgradeData data, OwnerData newOwner)
    {
        data.editData(ModDataComponents.SECURITY_UPGRADE_DATA,SecurityUpgradeData.DEFAULT,d -> d.withOwner(newOwner,be.registryAccess()));
    }

    @Override
    public boolean BlockAccess(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data, @Nonnull Player player) { return !this.isMember(be, data, player); }

    @Override
    public void OnEquip(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) { data.editData(ModDataComponents.SECURITY_UPGRADE_DATA, SecurityUpgradeData.DEFAULT,d -> d.withBreakIsValid(false)); }

    @Override
    public void OnValidBlockRemoval(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        data.editData(ModDataComponents.SECURITY_UPGRADE_DATA,SecurityUpgradeData.DEFAULT,d -> d.withBreakIsValid(true));
    }

    @Override
    public void OnBlockRemoval(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        if(data.getData(ModDataComponents.SECURITY_UPGRADE_DATA,SecurityUpgradeData.DEFAULT).breakIsValid)
            return;
        OwnerData owner = this.parseOwnerData(be, data);
        if(owner.hasOwner())
        {
            List<ItemStack> items = new ArrayList<>(be.getStorage().removeAllItems());
            items.addAll(be.getUpgrades().removeAllItems());
            EjectionSaveData.HandleEjectionData(be.getLevel(), be.getBlockPos(), EjectionData.create(be.getLevel(), be.getBlockPos(), be.getBlockState(), IDumpable.preCollected(items, be.getDisplayName(), owner), false));
        }
    }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { consumer.accept(new SecurityUpgradeTab(data,screen)); }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data) {
        List<Component> tooltip = LCText.TOOLTIP_UPGRADE_SECURITY.get();
        if(LCConfig.SERVER.isLoaded() && LCConfig.SERVER.anarchyMode.get())
            tooltip.add(LCText.TOOLTIP_ANARCHY_WARNING.get().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED));
        return tooltip;
    }

    @Override
    public boolean clearDataFromStack(@Nonnull ItemStack stack) { return this.clearData(stack, ModDataComponents.SECURITY_UPGRADE_DATA); }

}
