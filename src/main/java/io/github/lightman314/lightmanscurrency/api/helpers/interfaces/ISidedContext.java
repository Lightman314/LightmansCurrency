package io.github.lightman314.lightmanscurrency.api.helpers.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
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
    static ISidedContext wrap(Entity entity) { return () -> entity.level().isClientSide(); }
    static ISidedContext wrap(Item.TooltipContext context) {
        if(context.level() != null)
            return context.level()::isClientSide;
        if(context.player() != null)
            return wrap(context.player());
        return LOGICAL_CLIENT;
    }
    static ISidedContext wrapUnknown(@Nullable Object object) {
        return switch (object) {
            case ISidedContext c -> c;
            case Entity e -> wrap(e);
            case Level l -> l::isClientSide;
            case LevelChunk c -> c.getLevel()::isClientSide;
            case BlockEntity be -> () -> {
                Level l = be.getLevel();
                return l == null || l.isClientSide();
            };
            case Item.TooltipContext c -> wrap(c);
            default -> LOGICAL_CLIENT;
        };
    }

    interface Slave extends ISidedContext
    {
        ISidedContext getParentContext();
        @ApiStatus.NonExtendable
        default boolean isClient() { return this.getParentContext().isClient(); }
    }

    interface Mutable<T> extends ISidedContext {
        T setSidedContext(ISidedContext context);
    }

    static String getDebugSideName(ISidedContext context) { return context.isClient() ? "client" : "server"; }

}
