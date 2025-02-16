package io.github.lightman314.lightmanscurrency.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.lwjgl.system.NonnullDefault;

import java.util.ArrayList;
import java.util.List;

@NonnullDefault
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

}
