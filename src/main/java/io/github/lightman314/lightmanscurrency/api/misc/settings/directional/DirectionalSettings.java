package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DirectionalSettings {

    private final IDirectionalSettingsHolder parent;
    private final Map<Direction,DirectionalSettingsState> data = new HashMap<>();

    public DirectionalSettings(IDirectionalSettingsHolder parent) {
        this.parent = parent;
    }

    public DirectionalSettingsState getState(Direction side) { return this.parent.getIgnoredSides().contains(side) ? DirectionalSettingsState.NONE : this.data.getOrDefault(side,DirectionalSettingsState.NONE); }
    public void setState(Direction side,DirectionalSettingsState state) {
        if(this.parent.getIgnoredSides().contains(side))
            return;
        this.data.put(side,state);
    }

    public boolean allowInputs(Direction side) { return this.getState(side).allowsInputs(); }
    public boolean allowOutputs(Direction side) { return this.getState(side).allowsOutputs(); }

    public void save(CompoundTag compound, String tag) {
        ListTag list = new ListTag();
        for(Direction side : Direction.values())
        {
            if(this.parent.getIgnoredSides().contains(side) || this.getState(side) == DirectionalSettingsState.NONE)
                continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("Side",side.toString());
            entry.putString("State",this.getState(side).toString());
            list.add(entry);
        }
        compound.put(tag,list);
    }

    public void load(CompoundTag compound, String tag) {
        if(!compound.contains(tag))
            return;
        this.data.clear();
        ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            Direction side = EnumUtil.enumFromString(entry.getString("Side"),Direction.values(),null);
            DirectionalSettingsState state = DirectionalSettingsState.parse(entry.getString("State"));
            if(side != null && state != DirectionalSettingsState.NONE)
                this.data.put(side,state);
            else if(side == null)
                LightmansCurrency.LogWarning("Could not properly parse '" + entry.getString("Side") + "' as a valid side!");
        }
    }

    public void copy(DirectionalSettings other) {
        for(Direction side : Direction.values())
        {
            if(this.parent.getIgnoredSides().contains(side))
                continue;
            this.setState(side,other.getState(side));
        }
    }

    public void clear() { this.data.clear(); }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("DirectionalSettings[");
        boolean notFirst = false;
        for(Direction side : Direction.values())
        {
            if(this.parent.getIgnoredSides().contains(side))
                continue;
            DirectionalSettingsState state = this.getState(side);
            if(state != DirectionalSettingsState.NONE)
            {
                if(notFirst)
                    builder.append(",");
                else
                    notFirst = true;
                builder.append(side).append(':').append(state);
            }
        }
        return builder.append("]").toString();
    }
}
