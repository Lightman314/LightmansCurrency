package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public class ModAttachmentTypes {

    public static void init() {}

    public static final Supplier<AttachmentType<WalletHandler>> WALLET_HANDLER;
    public static final Supplier<AttachmentType<EventUnlocks>> EVENT_UNLOCKS;

    static {
        WALLET_HANDLER = ModRegistries.ATTACHMENT_TYPES.register("wallet", () -> AttachmentType.serializable(WalletHandler::create).copyOnDeath().build());
        EVENT_UNLOCKS = ModRegistries.ATTACHMENT_TYPES.register("event_unlocks", () -> AttachmentType.serializable(EventUnlocks::create).copyOnDeath().build());
    }

}
