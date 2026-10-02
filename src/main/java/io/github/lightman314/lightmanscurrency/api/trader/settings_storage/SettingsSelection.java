package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsSelection {

    private final Set<DualKey> data = new HashSet<>();

    private SettingsSelection(Collection<DualKey> data) { this.data.addAll(data); }
    public SettingsSelection(List<ISettingsStorageIO> options) {
        for(ISettingsStorageIO s : options)
            this.data.add(s.getSettingsKey());
    }

    public boolean getState(DualKey key) { return this.data.contains(key); }
    public boolean getState(ISettingsStorageIO entry) { return this.getState(entry.getSettingsKey()); }

    public List<ISettingsStorageIO> onlySelected(List<ISettingsStorageIO> settings) { return settings.stream().filter(this::getState).toList(); }

    public void toggleState(DualKey key,List<ISettingsStorageIO> settings) { this.setState(key,!this.getState(key),settings); }

    public void setState(DualKey key,boolean state,List<ISettingsStorageIO> settings) {
        if(state) {
            this.data.add(key);
            if(key.hasEmptyKey()) {
                //By default, select all sub-settings when a full setting is selected
                for(ISettingsStorageIO s : settings) {
                    DualKey sk = s.getSettingsKey();
                    if(!sk.hasEmptyKey() && sk.isSameType(key))
                        this.data.add(sk);
                }
            }
        }
        else {
            this.data.remove(key);
            //Remove child entries if the parent is also disabled
            if(key.hasEmptyKey())
                this.data.removeIf(k -> k.getType().equals(key.getType()));
        }
    }

    public FancyPacketMap.Mutable write(String key) { return this.write(key,FancyPacketMap.map()); }

    public FancyPacketMap.Mutable write(String key,FancyPacketMap.Mutable packet) {
        return packet.setList(key,LCFancyPacketTypes.DUAL_KEY,List.copyOf(this.data));
    }

    public static SettingsSelection read(String key, FancyPacketMap packet) {
        return new SettingsSelection(packet.getList(key,LCFancyPacketTypes.DUAL_KEY));
    }

}