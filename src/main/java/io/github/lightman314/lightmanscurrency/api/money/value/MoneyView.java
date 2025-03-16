package io.github.lightman314.lightmanscurrency.api.money.value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MoneyView {

    private static final MoneyView EMPTY = new MoneyView();

    private final Map<String,MoneyValue> values;

    private MoneyView() { this.values = ImmutableMap.of(); }
    private MoneyView(Builder builder) {
        Map<String,MoneyValue> results = new HashMap<>();
        builder.values.forEach((name,list) -> {
            if(!list.isEmpty())
            {
                MoneyValue firstVal = list.getFirst();
                MoneyValue sum = firstVal.getCurrency().sumValues(list);
                if(sum != null && !sum.isEmpty() && !sum.isFree() && !sum.isInvalid())
                    results.put(name, sum);
            }
        });
        this.values = ImmutableMap.copyOf(results);
    }

    
    public static MoneyView.Builder builder() { return new MoneyView.Builder(); }
    
    public static MoneyView singleton(MoneyValue value) { return builder().add(value).build(); }
    
    public static MoneyView empty() { return EMPTY; }

    
    public List<MoneyValue> allValues() { return ImmutableList.copyOf(this.values.values()); }

    /**
     * Returns the stored value with the name requested.
     */
    
    public MoneyValue valueOf(String uniqueName) { return this.values.getOrDefault(uniqueName, MoneyValue.empty()); }

    /**
     * Whether the given amount of money (or more) is currently stored in the value holder.
     */
    public boolean containsValue(MoneyValue value) { return this.valueOf(value.getUniqueName()).containsValue(value); }

    /**
     * Returns the maximum amount of money that can be taken
     */
    
    public MoneyValue capValue(MoneyValue value) { return this.containsValue(value) ? value : this.valueOf(value.getUniqueName()); }

    public boolean isEmpty() { return this.values.isEmpty() || this.values.values().stream().allMatch(MoneyValue::isEmpty); }

    public String getString()
    {
        StringBuilder builder = new StringBuilder();
        for(Component line : this.getAllText())
        {
            if(!builder.isEmpty())
                builder.append("\n");
            builder.append(line.getString());
        }
        return builder.toString();
    }

    public List<Component> getAllText(ChatFormatting... style)
    {
        List<Component> text = new ArrayList<>();
        for(CurrencyType type : MoneyAPI.API.AllCurrencyTypes())
            type.getGroupTooltip(this,line -> text.add(EasyText.makeMutable(line).withStyle(style)));
        return text;
    }

    public MoneyValue getRandomValue() {
        if(this.values.isEmpty())
            return MoneyValue.empty();
        return ListUtil.randomItemFromList(this.values.values().stream().toList(),MoneyValue.empty());
    }

    
    public MutableComponent getRandomValueText() { return this.getRandomValueText(LCText.GUI_MONEY_STORAGE_EMPTY.get()); }
    
    public MutableComponent getRandomValueText(String emptyText) { return this.getRandomValueText(EasyText.literal(emptyText)); }
    
    public MutableComponent getRandomValueText(MutableComponent emptyText)
    {
        if(this.values.isEmpty())
            return emptyText;
        return this.getRandomValue().getText();
    }

    public Component getRandomValueLine() { return this.getRandomValueLine(LCText.GUI_MONEY_STORAGE_EMPTY.get()); }
    public Component getRandomValueLine(String emptyText) { return this.getRandomValueLine(EasyText.literal(emptyText)); }
    public Component getRandomValueLine(MutableComponent emptyText) {
        if(this.values.isEmpty())
            return emptyText;
        return ListUtil.randomItemFromList(this.getAllText(),EasyText.empty());
    }

    public static final class Builder
    {

        private final Map<String,List<MoneyValue>> values = new HashMap<>();

        private Builder() {}

        public Builder merge(IMoneyViewer storage) { this.add(storage.getStoredMoney().allValues()); return this; }
        public Builder merge(Builder values) { values.values.forEach((name,list) -> this.add(list)); return this; }
        public Builder merge(MoneyView values) { this.add(values.allValues()); return this; }

        /**
         * Adds the given {@link MoneyValue}.
         */
        public Builder add(MoneyValue value)
        {
            if(value.isEmpty() || value.isFree() || value.isInvalid())
                return this;
            String name = value.getUniqueName();
            List<MoneyValue> list = this.values.getOrDefault(name, new ArrayList<>());
            list.add(value);
            if(!this.values.containsKey(name))
                this.values.put(name, list);
            return this;
        }

        /**
         * Adds all the given {@link MoneyValue MoneyValues}.
         */
        public Builder add(Collection<MoneyValue> values)
        {
            for(MoneyValue val : values)
            {
                if(val != null)
                    this.add(val);
            }
            return this;
        }

        public MoneyView build() { return new MoneyView(this); }

    }

}