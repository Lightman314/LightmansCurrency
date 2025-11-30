package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public class ModAttachmentTypes {

    public static void init() {}

    public static final Supplier<AttachmentType<WalletHandler>> WALLET_HANDLER;
    public static final Supplier<AttachmentType<EventUnlocks>> EVENT_UNLOCKS;
    public static final Supplier<AttachmentType<VariantDataStorageAttachment>> VARIANT_BLOCK_DATA;
    public static final Supplier<AttachmentType<VariantChunkDataStorageAttachment>> VARIANT_CHUNK_DATA;

    static {
        WALLET_HANDLER = ModRegistries.ATTACHMENT_TYPES.register("wallet", () -> AttachmentType.serializable(WalletHandler::create).copyOnDeath().build());
        EVENT_UNLOCKS = ModRegistries.ATTACHMENT_TYPES.register("event_unlocks", () -> AttachmentType.serializable(EventUnlocks::create).copyOnDeath().build());
        VARIANT_BLOCK_DATA = ModRegistries.ATTACHMENT_TYPES.register("variant_block_data", () -> AttachmentType.builder(VariantDataStorageAttachment::new).serialize(VariantDataStorageAttachment.SERIALIZER).sync(VariantDataStorageAttachment.SYNC_HANDLER).build());
        VARIANT_CHUNK_DATA = ModRegistries.ATTACHMENT_TYPES.register("variant_chunk_data", () -> AttachmentType.builder(VariantChunkDataStorageAttachment::new).serialize(VariantChunkDataStorageAttachment.SERIALIZER).sync(VariantChunkDataStorageAttachment.SYNC_HANDLER).build());
    }

}
