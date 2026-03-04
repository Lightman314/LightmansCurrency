package io.github.lightman314.lightmanscurrency.api.traders.data.interfaces;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface IPersistentTrader {

    boolean isPersistent();
    String getPersistentID();
    void makePersistent(long id, String persistentID) throws IllegalStateException;

    JsonObject writePersistentJson(String id, String owner, DataContext<JsonElement> context);
    void readPersistentJson(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException;

    CompoundTag savePersistentData(DataContext<Tag> context);
    void loadPersistentData(CompoundTag compound, DataContext<Tag> context);

}