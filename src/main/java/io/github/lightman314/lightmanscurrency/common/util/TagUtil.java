package io.github.lightman314.lightmanscurrency.common.util;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TagUtil {

    public static CompoundTag saveBlockPos(BlockPos pos)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static BlockPos loadBlockPos(CompoundTag tag) { return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")); }

    public static ListTag writeStringList(List<String> list)
    {
        ListTag result = new ListTag();
        for(String value : list)
            result.add(StringTag.valueOf(value));
        return result;
    }

    public static List<String> loadStringList(ListTag list)
    {
        List<String> result = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
            result.add(list.getString(i));
        return result;
    }

    public static ListTag writeUUIDList(List<UUID> list)
    {
        ListTag result = new ListTag();
        for(UUID value : list)
            result.add(NbtUtils.createUUID(value));
        return result;
    }

    public static List<UUID> readUUIDList(ListTag list)
    {
        List<UUID> result = new ArrayList<>();
        for(Tag tag : list)
            result.add(NbtUtils.loadUUID(tag));
        return result;
    }

    public static ListTag writeIconList(NonNullList<IconData> list, HolderLookup.Provider lookup)
    {
        ListTag iconList = new ListTag();
        for(IconData icon : list)
            iconList.add(icon.save(lookup));
        return iconList;
    }

    public static void readIconList(NonNullList<IconData> list, ListTag listTag, HolderLookup.Provider lookup, IconData defaultIcon)
    {
        list.clear();
        for(int i = 0; i < list.size() && i < listTag.size(); ++i)
            list.set(i,IconData.safeLoad(listTag.getCompound(i),lookup,defaultIcon));
    }

    public static String writeModelResource(ModelResourceLocation modelID)
    {
        //Only save the id if it's a standalone
        if(modelID.getVariant().equals(ModelResourceLocation.STANDALONE_VARIANT))
            return modelID.id().toString();
            //If it's not a standalone, save the entire model id including variant
        else
            return modelID.toString();
    }

    public static ModelResourceLocation readModelResource(String modelID)
    {
        if(modelID.contains("#"))
        {
            String[] split = modelID.split("#",2);
            return new ModelResourceLocation(VersionUtil.parseResource(split[0]),split[1]);
        }
        else
            return ModelResourceLocation.standalone(VersionUtil.parseResource(modelID));
    }

}
