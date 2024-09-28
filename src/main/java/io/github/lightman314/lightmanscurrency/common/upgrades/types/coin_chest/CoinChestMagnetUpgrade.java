package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class CoinChestMagnetUpgrade extends TickableCoinChestUpgrade {

    @Override
    public boolean isUnique() { return true; }

    public static final String RANGE = "magnet_range";

    @Override
    public void HandleMenuMessage(@Nonnull CoinChestMenu menu, @Nonnull CoinChestUpgradeData data, @Nonnull LazyPacketData message) { }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { }

    public int getRadius(CoinChestUpgradeData data) { return data.getUpgradeData().getIntValue(RANGE); }

    @Override
    public void OnServerTick(@Nonnull CoinChestBlockEntity be, @Nonnull CoinChestUpgradeData data) {
        int radius = this.getRadius(data);
        Vector3f pos = be.getBlockPos().getCenter().toVector3f();
        AABB searchBox = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        boolean playSound = false;
        for(Entity e : be.getLevel().getEntities((Entity)null, searchBox, e -> e instanceof ItemEntity item && CoinAPI.API.IsCoin(item.getItem(), true)))
        {
            ItemEntity ie = (ItemEntity)e;
            ItemStack coinStack = ie.getItem();
            ItemStack leftovers = InventoryUtil.TryPutItemStack(be.getStorage(), coinStack);
            if(leftovers.getCount() != coinStack.getCount())
            {
                playSound = true;
                if(leftovers.isEmpty())
                    ie.discard();
                else
                    ie.setItem(leftovers);
            }
        }
        if(playSound)
            be.getLevel().playSound(null, be.getBlockPos(), ModSounds.COINS_CLINKING.get(), SoundSource.PLAYERS, 0.4f, 1f);
    }

    @Nonnull
    @Override
    public List<Component> getTooltip(@Nonnull UpgradeData data) { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_MAGNET.get(data.getIntValue(RANGE))); }

}
