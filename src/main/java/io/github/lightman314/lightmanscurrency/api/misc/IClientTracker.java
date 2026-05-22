package io.github.lightman314.lightmanscurrency.api.misc;

import io.github.lightman314.lightmanscurrency.common.attachments.EasyAttachment;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Supplier;

public interface IClientTracker {

	boolean isClient();
	default boolean isServer() { return !this.isClient(); }

	static IClientTracker entityWrapper(Entity entity) { return () -> entity.level().isClientSide; }
	static IClientTracker forKnown(boolean isClient) { return () -> isClient; }
	static IClientTracker forClient() { return () -> true; }
	static IClientTracker forServer() { return () -> false; }
    static IClientTracker forAttachment(Supplier<IAttachmentHolder> holder) { return () -> forAttachment(holder.get()).isClient(); }
    static IClientTracker forAttachment(IAttachmentHolder holder) { return () -> EasyAttachment.isClient(holder); }

    interface Slave extends IClientTracker
    {
        IClientTracker getParentTracker();
        default boolean isClient() {
            IClientTracker parent = this.getParentTracker();
            return parent == null || parent.isClient();
        }
    }

}