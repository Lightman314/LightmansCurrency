package io.github.lightman314.lightmanscurrency.api.traders.rules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public interface IPersistentRule {

    @Nullable
    JsonObject writePersistentData(DataContext<JsonElement> context);
    void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException;

    @Nullable
    default CompoundTag writePersistentTag(DataContext<Tag> context) { return null; }
    default void readPersistentTag(CompoundTag tag,DataContext<Tag> context) {}

}
