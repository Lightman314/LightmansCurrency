package io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHolder;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Simple holder of multiple {@link MoneyValue} values that allows for infinite storage.
 * Typically used for Trader Money Storage, Bank Accounts, Tax Collectors, etc.
 */
public final class MoneyStorage extends MoneyHolder {

    public static final Codec<MoneyStorage> CODEC = Codec.withAlternative(
            MoneyValue.CODEC.listOf().xmap(MoneyStorage::new, MoneyStorage::allValues),
            CodecHelper.oldValueListLoader(MoneyStorage::loadOld,"Money Storage"));

    public static final StreamCodec<RegistryFriendlyByteBuf,MoneyStorage> STREAM_CODEC = MoneyValue.STREAM_CODEC.apply(ByteBufCodecs.list()).map(MoneyStorage::new,MoneyStorage::allValues);

    private final Map<String,MoneyValue> values = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private int priority;

    private List<MoneyValue> getValues() { return new ArrayList<>(this.values.values()); }

    private MoneyStorage(List<MoneyValue> values)
    {
        for(MoneyValue value : values)
            this.addValue(value);
    }
    public MoneyStorage() { }

    public MoneyStorage withListener(Runnable listener) { this.listeners.add(listener); return this; }
    public MoneyStorage withPriority(int priority) { this.priority = priority; return this;}

    private void markDirty() {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }

    @Override
    public int priority() { return this.priority; }

    public List<MoneyValue> allValues() { return ImmutableList.copyOf(this.values.values()); }

    /**
     * Returns the stored value with the given unique name
     */
    public MoneyValue valueOf(String uniqueName) { return this.values.getOrDefault(uniqueName, MoneyValue.empty()); }

    
    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        if(!simulation)
            this.addValue(insertAmount);
        return MoneyValue.empty();
    }

    /**
     * Adds/deposits the given {@link MoneyValue} to the currently stored money.
     */
    public void addValue(MoneyValue value)
    {
        if(value.isEmpty() || value.isFree())
            return;
        String name = value.getUniqueName();
        if(this.values.containsKey(name) && !this.values.get(name).isInvalid())
        {
            MoneyValue oldValue = this.values.get(name);
            MoneyValue addedValue = oldValue.addValue(value);
            if(addedValue != null)
                this.values.put(name, addedValue);
            else
            {
                LightmansCurrency.LogError("Error adding money values of type '" + oldValue.getType() + "' and '" + value.getType() + "' together.");
                return;
            }
        }
        else
            this.values.put(name, value);
        this.markDirty();
    }

    /**
     * Adds/deposits all the given {@link MoneyValue} to the currently stored money.
     */
    public void addValues(Collection<MoneyValue> values)
    {
        for(MoneyValue val : values)
        {
            if(val != null)
                this.addValue(val);
        }
    }

    /**
     * Whether the given amount of money (or more) is currently stored in the value holder.
     */
    public boolean containsValue(MoneyValue value) { return this.valueOf(value.getUniqueName()).containsValue(value); }

    /**
     * Returns the maximum amount of money that can be taken
     */
    
    public MoneyValue capValue(MoneyValue value) { return this.containsValue(value) ? value : this.valueOf(value.getUniqueName()); }


    public boolean isEmpty() { return this.values.isEmpty() || this.values.values().stream().allMatch(MoneyValue::isEmpty); }

    /**
     * Removes the requested {@link MoneyValue} from the stored money.
     * Throws {@link IllegalArgumentException} if the amount to remove is larger than the amount stored,
     * so it's recommended to check {@link #containsValue(MoneyValue)} before executing.
     */
    public void removeValue(MoneyValue value)
    {
        if(value.isFree() || value.isEmpty())
            return;
        if(!this.containsValue(value))
            throw new IllegalArgumentException("Cannot remove more money than is stored in the holder!");
        String name = value.getUniqueName();
        MoneyValue oldValue = this.values.get(name);
        MoneyValue newValue = oldValue.subtractValue(value);
        if(newValue == null)
        {
            LightmansCurrency.LogError("Error subtracting money values of type '" + oldValue.getType() + "' and '" + value.getType() + "' together.");
            return;
        }
        else if(newValue.isEmpty() || newValue.isFree())
            this.values.remove(name);
        else
            this.values.put(name, newValue);
        this.markDirty();
    }

    
    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
        MoneyValue actualRemoved = this.capValue(extractAmount);
        if(!simulation)
            this.removeValue(actualRemoved);
        return extractAmount.subtractValue(actualRemoved);
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return true; }

    public ListTag save() { return (ListTag)CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow(); }

    private static MoneyStorage loadOld(ListTag list)
    {
        MoneyStorage result = new MoneyStorage();
        for(int i = 0; i < list.size(); ++i)
        {
            MoneyValue value = MoneyValue.load(list.getCompound(i));
            result.values.put(value.getUniqueName(), value);
        }
        return result;
    }

    public void load(ListTag list)
    {
        this.values.clear();
        this.values.putAll(CODEC.decode(NbtOps.INSTANCE,list).getOrThrow().getFirst().values);
    }

    public void load(List<MoneyValue> values)
    {
        this.values.clear();
        for(MoneyValue v : values)
            this.values.put(v.getUniqueName(),v);
    }

    public void clear() { this.values.clear(); this.markDirty(); }

    
    public Component getRandomValueText() { return this.getRandomValueText(LCText.GUI_MONEY_STORAGE_EMPTY.get()); }
    
    public Component getRandomValueText(String emptyText) { return this.getRandomValueText(EasyText.literal(emptyText)); }
    
    public Component getRandomValueText(Component emptyText)
    {
        if(this.values.isEmpty())
            return emptyText;
        List<MoneyValue> values = this.values.values().stream().toList();
        return ListUtil.randomItemFromList(values,MoneyValue.empty()).getText();
    }

    public Component getAllValueText() {
        MutableComponent text = EasyText.empty();
        for(MoneyValue value : this.values.values())
        {
            if(value.isEmpty())
                continue;
            if(!text.getString().isEmpty())
                text.append(LCText.GUI_SEPERATOR.get());
            text.append(value.getText());
        }
        return text;
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        for(MoneyValue value : this.values.values())
            builder.add(value);
    }

    @Override
    public Component getTooltipTitle() { return LCText.TOOLTIP_MONEY_SOURCE_STORAGE.get(); }

    public void GiveToPlayer(Player player)
    {
        IMoneyHolder handler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
        List<MoneyValue> extra = new ArrayList<>();
        for(MoneyValue value : this.allValues())
        {
            MoneyValue e = handler.insertMoney(value,false);
            if(!e.isEmpty())
                extra.add(e);
        }
        this.clear();
        for(MoneyValue e : extra)
            this.addValue(e);
        this.markDirty();
    }

}
