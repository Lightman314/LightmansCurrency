package io.github.lightman314.lightmanscurrency.api.trader.rules;

import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

//TODO fill out when persistent traders get implemented
public interface IPersistentRule {

    void writePersistentData(ValueOutput output) throws PersistentDataException;
    void loadPersistentData(ValueInput input) throws PersistentDataException;

    default void writePersistentSaveData(ValueOutput ouput) {}
    default void readPersisentSaveData(ValueInput input) {}

}