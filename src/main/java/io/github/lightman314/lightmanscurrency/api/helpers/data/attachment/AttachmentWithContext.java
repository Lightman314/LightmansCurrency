package io.github.lightman314.lightmanscurrency.api.helpers.data.attachment;

import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AttachmentWithContext {

    void attachHolder(IAttachmentHolder holder);
    IAttachmentHolder getHolder();

    static <T extends AttachmentWithContext> Function<IAttachmentHolder,T> factoryWithContext(Supplier<T> factory) {
        return holder -> {
            T value = factory.get();
            value.attachHolder(holder);
            return value;
        };
    }

}
