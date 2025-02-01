package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.CapabilityEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.client.AncientCoinValueInput;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers.AncientContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers.AncientPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AncientMoneyType extends CurrencyType {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("ancient_coins");
    public static final AncientMoneyType INSTANCE = new AncientMoneyType();

    private AncientMoneyType() { super(TYPE); }

    @Nonnull
    @Override
    protected MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values) {
        Map<AncientCoinType,Integer> map = new HashMap<>();
        AncientCoinType ancientCoinType = null;
        long count = 0;
        for(MoneyValue v : values)
        {
            if(v instanceof AncientMoneyValue value)
            {
                if(ancientCoinType == null)
                    ancientCoinType = value.type;
                if(value.type == ancientCoinType)
                    count += value.count;
            }
        }
        return AncientMoneyValue.of(ancientCoinType,count);
    }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(@Nonnull Player player) { return new AncientPlayerMoneyHandler(player); }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler, @Nonnull IClientTracker tracker) { return new AncientContainerMoneyHandler(container,overflowHandler); }

    @Override
    public MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag) { return AncientMoneyValue.load(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(@Nonnull JsonObject json) { return AncientMoneyValue.loadFromJson(json); }

    @Nonnull
    @Override
    public MoneyValueParser getValueParser() { return AncientMoneyParser.INSTANCE; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Object> getInputHandlers(@Nullable Player player) {
        if(CapabilityEventUnlocks.isUnlocked(player, "ancient_coins") || LCAdminMode.isAdminPlayer(player))
            return List.of(new AncientCoinValueInput());
        return List.of();
    }

}