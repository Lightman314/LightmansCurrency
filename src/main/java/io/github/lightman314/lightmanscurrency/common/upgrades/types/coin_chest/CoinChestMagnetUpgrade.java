package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
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
import java.util.Objects;
import java.util.function.Consumer;

public class CoinChestMagnetUpgrade extends TickableCoinChestUpgrade {

    public static final String RANGE = "magnet_range";

    @Override
    public void HandleMenuMessage(CoinChestMenu menu, CoinChestUpgradeData data, LazyPacketData message) { }

    @Override
    public void addClientTabs(@Nonnull CoinChestUpgradeData data, @Nonnull Object screen, @Nonnull Consumer<Object> consumer) { }

    public int getRadius(CoinChestUpgradeData data) { return data.getUpgradeData().getIntValue(RANGE); }

    @Override
    public void OnServerTick(CoinChestBlockEntity be, CoinChestUpgradeData data) {
        int radius = this.getRadius(data);
        Vector3f pos = be.getBlockPos().getCenter().toVector3f();
        AABB searchBox = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        boolean playSound = false;
        for(Entity e : be.getLevel().getEntities((Entity)null, searchBox, e -> e instanceof ItemEntity item && MoneyUtil.isCoin(item.getItem(), true)))
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

    @Override
    protected List<String> getDataTags() { return ImmutableList.of(RANGE); }

    @Override
    protected Object defaultTagValue(String tag) {
        if(Objects.equals(tag, RANGE))
            return 1;
        return null;
    }

    @Override
    public List<Component> getTooltip(UpgradeData data) { return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.upgrade.coin_chest.magnet", data.getIntValue(RANGE))); }

}
