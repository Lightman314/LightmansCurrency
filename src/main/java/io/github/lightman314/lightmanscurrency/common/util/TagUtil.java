package io.github.lightman314.lightmanscurrency.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.lwjgl.system.NonnullDefault;

@NonnullDefault
public class TagUtil {

    public static CompoundTag saveBlockPos( BlockPos pos)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static BlockPos loadBlockPos(CompoundTag tag) { return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")); }

}
