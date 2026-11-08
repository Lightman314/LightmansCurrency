package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface IPersistentNode {

    void writePersistentData(ValueOutput output,String nameOverride) throws PersistentDataException;
    void readPersistentData(ValueInput input,ProblemReporter reporter) throws PersistentDataException;

    default void writePersistentSaveData(ValueOutput output) {}
    default void readPersisentSaveData(ValueInput input) {}

}