package io.github.lightman314.lightmanscurrency.api.misc.data.variables.basic;

import io.github.lightman314.lightmanscurrency.api.misc.data.variables.EasyVariable;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IntVariable extends EasyVariable<Integer> {

    private int value;
    private final int minValue;
    private final int maxValue;

    public IntVariable(int defaultValue, Builder builder) { this(defaultValue,Integer.MIN_VALUE,Integer.MAX_VALUE,builder); }
    public IntVariable(int defaultValue, int minValue, int maxValue, Builder builder) {
        super(builder);
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Integer get() { return this.value; }

    @Override
    protected void setInternal(Integer newValue) {
        newValue = MathUtil.clamp(newValue,this.minValue,this.maxValue);
        if(this.value != newValue)
        {
            this.value = newValue;
            this.setChanged();
        }
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) { tag.putInt(this.key,this.value); }
    @Override
    public void load(CompoundTag tag, HolderLookup.Provider lookup) { this.value = tag.getInt(this.key); }

}
