package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers.AncientContainerMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers.AncientPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AncientMoneyType extends CurrencyType {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("ancient_coins");
    public static final AncientMoneyType INSTANCE = new AncientMoneyType();

    private static final Comparator<AncientMoneyValue> TYPE_SORTER = new TypeSorter();

    private AncientMoneyType() { super(TYPE); }
    
    @Override
    protected MoneyValue sumValuesInternal(List<MoneyValue> values) {
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

    @Override
    public void getGroupTooltip(MoneyView money, Consumer<MutableComponent> lineConsumer) {
        List<AncientMoneyValue> ancientMoney = new ArrayList<>();
        for(MoneyValue value : money.allValues())
        {
            if(value instanceof AncientMoneyValue av && !av.isEmpty())
                ancientMoney.add(av);
        }
        MutableComponent line = EasyText.empty();
        int lineSize = 0;
        ancientMoney.sort(TYPE_SORTER);
        for(AncientMoneyValue val : ancientMoney)
        {
            if(lineSize > 0)
                line.append(LCText.GUI_SEPERATOR.get());
            line.append(val.getText());
            if(++lineSize >= 3)
            {
                lineConsumer.accept(line);
                lineSize = 0;
                line = EasyText.empty();
            }
        }
        if(lineSize > 0)
            lineConsumer.accept(line);
    }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player) { return new AncientPlayerMoneyHandler(player); }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return new AncientContainerMoneyHandler(container,overflowHandler); }

    @Override
    public MoneyValue loadMoneyValue(CompoundTag valueTag) { return AncientMoneyValue.load(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(JsonObject json) { return AncientMoneyValue.loadFromJson(json); }

    @Override
    public MoneyValueParser getValueParser() { return AncientMoneyParser.INSTANCE; }

    @Override
    public boolean allowItemInMoneySlot(Player player, ItemStack item) { return item.getItem() == ModItems.COIN_ANCIENT.get(); }

    private static class TypeSorter implements Comparator<AncientMoneyValue>
    {
        @Override
        public int compare(AncientMoneyValue val1, AncientMoneyValue val2) {
            AncientCoinType type1 = val1.type;
            AncientCoinType type2 = val2.type;
            //If they're both netherite coins, sort in order
            if(type1.tag.equals(type2.tag))
                return Integer.compare(type1.ordinal(),type2.ordinal());
            else //Otherwise sort inverted
                return Integer.compare(type2.ordinal(),type1.ordinal());
        }
    }

}
