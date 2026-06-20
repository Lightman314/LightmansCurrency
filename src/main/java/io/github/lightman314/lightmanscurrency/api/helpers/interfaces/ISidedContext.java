package io.github.lightman314.lightmanscurrency.api.helpers.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Interface applied to various classes so that the logical side can be more easily obtained
 */
public interface ISidedContext {

    ISidedContext LOGICAL_CLIENT = () -> true;
    ISidedContext LOGICAL_SERVER = () -> false;

    boolean isClient();
    @ApiStatus.NonExtendable
    default boolean isServer() { return !this.isClient(); }

    static ISidedContext known(boolean isClient) { return isClient ? LOGICAL_CLIENT : LOGICAL_SERVER; }
    static ISidedContext wrap(Entity entity) { return entity == null ? LOGICAL_CLIENT : () -> entity.level().isClientSide(); }
    static ISidedContext wrapUnknown(@Nullable Object object) {
        if(object == null)
            return LOGICAL_CLIENT;
        if(object instanceof ISidedContext c)
            return c;
        if(object instanceof Entity e)
            return () -> e.level().isClientSide();
        if(object instanceof Level l)
            return l::isClientSide;
        if(object instanceof LevelChunk c)
            return c.getLevel()::isClientSide;
        if(object instanceof BlockEntity b)
            return () -> {
                Level l = b.getLevel();
                return l == null || l.isClientSide();
            };
        return LOGICAL_CLIENT;
    }

    interface Slave extends ISidedContext
    {
        ISidedContext getParentContext();
        @ApiStatus.NonExtendable
        default boolean isClient() {
            ISidedContext parent = this.getParentContext();
            return parent == null || parent.isClient();
        }
    }

    interface Mutable<T> extends ISidedContext
    {
        T setSidedContext(ISidedContext context);
    }

}
