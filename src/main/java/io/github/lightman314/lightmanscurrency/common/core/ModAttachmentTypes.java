package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.common.attachments.EasyAttachment;
import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachmentTypes {

    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,LightmansCurrency.MODID);

    public static final Supplier<AttachmentType<WalletHandler>> WALLET_HANDLER = register("wallet",() -> EasyAttachment.buildType(
            WalletHandler::new,
            WalletHandler.CODEC,
            WalletHandler.STREAM_CODEC,
            WalletHandler.COPIER));
    public static final Supplier<AttachmentType<EventUnlocks>> EVENT_UNLOCKS = register("event_unlocks",() -> EasyAttachment.buildType(
            EventUnlocks::new,
            EventUnlocks.CODEC,
            EventUnlocks.STREAM_CODEC,
            EventUnlocks.COPIER));

    public static final Supplier<AttachmentType<VariantDataStorageAttachment>> VARIANT_BLOCK_DATA = register("variant_block_data",() ->
            AttachmentType.builder(VariantDataStorageAttachment::new)
            .serialize(VariantDataStorageAttachment.SERIALIZER)
            .sync(VariantDataStorageAttachment.SYNC_HANDLER));
    public static final Supplier<AttachmentType<VariantChunkDataStorageAttachment>> VARIANT_CHUNK_DATA = register("variant_chunk_data", () ->
            AttachmentType.builder(VariantChunkDataStorageAttachment::new)
            .serialize(VariantChunkDataStorageAttachment.SERIALIZER)
            .sync(VariantChunkDataStorageAttachment.SYNC_HANDLER));

    public static <T> DeferredHolder<AttachmentType<?>,AttachmentType<T>> register(String id,Supplier<AttachmentType.Builder<T>> builder) {
        return REGISTER.register(id,() -> builder.get().build());
    }

}
