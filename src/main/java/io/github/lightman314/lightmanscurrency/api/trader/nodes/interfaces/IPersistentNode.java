package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface IPersistentNode {

    void writePersistentData(ValueOutput output,String id,String ownerName);
    void loadPersistentData(ValueInput input) throws PersistentDataException;

    default boolean writePersistentSaveData(ValueOutput ouput) { return false; }
    default void readPersisentSaveData(ValueInput input) {}

}