package io.github.lightman314.lightmanscurrency.integration.discord.data;

import io.github.lightman314.lightmansdiscord.discord.links.AccountManager;
import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurrencyBotData {

    public final String discordAccount;
    @Nullable
    public final LinkedAccount getLinkedAccount() { return AccountManager.getLinkedAccountFromDiscordID(this.discordAccount); }
    private boolean notificationsToDiscord = false;
    public boolean sendNotificationsToDiscord() { return this.notificationsToDiscord; }
    public void setNotificationsToDiscord(boolean newValue) { this.notificationsToDiscord = newValue; }

    public CurrencyBotData(@Nonnull String accountID) { this.discordAccount = accountID; }

    public CurrencyBotData(@Nonnull CompoundTag tag) {

        this(tag.getString("Account"));

        if(tag.contains("NotificationsToDiscord"))
            this.notificationsToDiscord = tag.getBoolean("NotificationsToDiscord");
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("Account", this.discordAccount);
        tag.putBoolean("NotificationsToDiscord", this.notificationsToDiscord);
        return tag;
    }

}