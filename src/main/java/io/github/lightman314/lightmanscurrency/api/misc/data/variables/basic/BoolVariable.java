package io.github.lightman314.lightmanscurrency.api.misc.data.variables.basic;

import io.github.lightman314.lightmanscurrency.api.misc.data.variables.EasyVariable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BoolVariable extends EasyVariable<Boolean> {

    private boolean value;

    public BoolVariable(boolean defaultValue,Builder builder) { super(builder); this.value = defaultValue; }

    @Override
    public Boolean get() { return this.value; }

    @Override
    protected void setInternal(Boolean newValue) {
        if(this.value != newValue)
        {
            this.value = newValue;
            this.setChanged();
        }
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) { tag.putBoolean(this.key,this.value); }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider lookup) { this.value = tag.getBoolean(this.key); }

}