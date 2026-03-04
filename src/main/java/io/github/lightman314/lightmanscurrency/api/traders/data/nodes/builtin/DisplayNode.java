package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.DisplaySettings;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DisplayNode extends SyncedTraderNode implements IPersistentNode {

    public static final int MAX_NAME_LENGTH = 32;

    private static final MapCodec<DisplayNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.either(Codec.STRING,ComponentSerialization.CODEC).optionalFieldOf("customName").forGetter(DisplayNode::getCustomNameEither),
            IconData.CODEC.optionalFieldOf("customIcon").forGetter(n -> n.customIcon),
            Codec.BOOL.fieldOf("showSearchBox").forGetter(DisplayNode::alwaysShowSearchBox)
    ).apply(builder,DisplayNode::new));

    public static final TraderNodeType<DisplayNode> TYPE = TraderNodeType.simple(DisplayNode::new,MAP_CODEC);

    private String customName = "";
    @Nullable
    private Component customNameText = null;
    public boolean hasCustomName() { return this.customNameText != null || !this.customName.isBlank(); }
    public Component getCustomName() {
        if(this.customNameText != null)
            return this.customNameText.copy();
        return EasyText.literal(this.customName);
    }
    public String getInternalCustomName() { return this.customName; }
    @Nullable
    public Component getInternalCustomNameText() { return this.customNameText; }
    public boolean setCustomName(@Nullable PlayerReference admin, String newName)
    {
        boolean different = this.customNameText != null;
        if(this.customNameText == null)
            different = !this.customName.equals(newName);
        if(different)
        {
            this.customNameText = null;
            this.customName = newName;
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NAME.get(),this.customName));
            //Set changed
            this.setChanged(builder -> builder.setString("customName",this.customName).remove("customNameText"));
            return true;
        }
        return false;
    }
    public boolean setCustomName(@Nullable PlayerReference admin, Component newName)
    {
        boolean different = true;
        if(this.customNameText == null && newName.getContents() instanceof PlainTextContents.LiteralContents contents)
            different = !this.customName.equals(contents.text());
        if(this.customNameText != null)
            different = !this.customNameText.equals(newName);
        if(different)
        {
            this.customName = "";
            this.customNameText = newName;
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NAME.get(),this.customNameText));
            this.setChanged(builder -> builder.setText("customNameText",this.customNameText).remove("customName"));
            return true;
        }
        return false;
    }
    private Optional<Either<String,Component>> getCustomNameEither() {
        if(this.hasCustomName())
        {
            if(this.customNameText != null)
                return Optional.of(Either.right(this.customNameText));
            return Optional.of(Either.left(this.customName));
        }
        return Optional.empty();
    }

    private Optional<IconData> customIcon = Optional.empty();
    public boolean hasCustomIcon() { return this.customIcon.isPresent(); }
    public IconData getCustomIcon() { return this.customIcon.orElse(IconData.Null()); }
    public boolean setCustomIcon(@Nullable PlayerReference admin,@Nullable IconData newIcon)
    {
        if(newIcon != null && newIcon.isNull())
            newIcon = null;
        Optional<IconData> newOptional = Optional.ofNullable(newIcon);
        if(newOptional.equals(this.customIcon))
            return false;
        this.customIcon = Optional.ofNullable(newIcon);
        if(admin != null)
            this.pushLocalNotification(ChangeSettingNotification.dumb(admin,LCText.DATA_ENTRY_TRADER_ICON.get()));
        this.setChanged(builder -> {
            builder.setBoolean("customIcon",this.customIcon.isPresent());
            this.customIcon.ifPresent(icon -> builder.setCustom("icon",icon,ModLazyPackets.ICON));
        });
        return true;
    }

    private boolean alwaysShowSearchBox = false;
    public final boolean alwaysShowSearchBox() { return this.alwaysShowSearchBox; }
    public final boolean setAlwaysShowSearchBox(@Nullable PlayerReference admin, boolean newVal)
    {
        if(this.alwaysShowSearchBox != newVal)
        {
            this.alwaysShowSearchBox = newVal;
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin,LCText.DATA_ENTRY_TRADER_ALWAYS_SHOW_SEARCH_BOX.get(),this.alwaysShowSearchBox));
            this.setChanged(builder -> builder.setBoolean("showSearchBox",this.alwaysShowSearchBox));
            return true;
        }
        return false;
    }

    private DisplayNode() {}
    private DisplayNode(Optional<Either<String,Component>> customName,Optional<IconData> customIcon,boolean alwaysShowSearchBox) {
        customName.ifPresent(either ->
                either.ifLeft(n -> this.customName = n)
                        .ifRight(c -> this.customNameText = c));
        this.customIcon = customIcon;
        this.alwaysShowSearchBox = alwaysShowSearchBox;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        if(this.customNameText != null)
            builder.setText("customNameText",this.customNameText);
        else
            builder.setString("customName",this.customName);
        builder.setBoolean("customIcon",this.customIcon.isPresent());
        this.customIcon.ifPresent(icon -> builder.setCustom("icon",icon,ModLazyPackets.ICON));
        builder.setBoolean("showSearchBox",this.alwaysShowSearchBox);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("customName"))
        {
            this.customNameText = null;
            this.customName = data.getString("customName");
        }
        if(data.contains("customNameText"))
        {
            this.customName = "";
            this.customNameText = data.getText("customNameText");
        }
        if(data.contains("customIcon"))
        {
            if(data.getBoolean("customIcon"))
                this.customIcon = Optional.ofNullable(data.getCustom("icon",ModLazyPackets.ICON));
            else
                this.customIcon = Optional.empty();
        }
        if(data.contains("showSearchBox"))
            this.alwaysShowSearchBox = data.getBoolean("showSearchBox");
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("ChangeName"))
        {
            if(this.hasPermission(player, Permissions.CHANGE_NAME))
            {
                String newName = message.getString("ChangeName");
                if(newName.length() > MAX_NAME_LENGTH)
                    newName = newName.substring(0,MAX_NAME_LENGTH);
                this.setCustomName(PlayerReference.of(player),newName);
            }
        }
        if(message.contains("ChangeIcon"))
        {
            if(this.hasPermission(player,Permissions.CHANGE_NAME))
            {
                IconData newIcon = message.getCustom("ChangeIcon",ModLazyPackets.ICON);
                this.setCustomIcon(PlayerReference.of(player),newIcon);
            }
        }
        if(message.contains("AlwaysShowSearchBox"))
        {
            if(this.hasPermission(player, Permissions.EDIT_SETTINGS))
            {
                boolean newVal = message.getBoolean("AlwaysShowSearchBox");
                this.setAlwaysShowSearchBox(PlayerReference.of(player),newVal);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Display Settings
        if(tag.contains("Name"))
        {
            this.customName = tag.getString("Name");
            this.customNameText = null;
        }
        if(tag.contains("CustomIcon"))
            this.customIcon = Optional.ofNullable(IconData.loadOldData(tag.getCompound("CustomIcon"),lookup));
        //Misc Settings
        if(tag.contains("AlwaysShowSearchBox"))
            this.alwaysShowSearchBox = tag.getBoolean("AlwaysShowSearchBox");
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        if(this.customNameText != null)
            json.add("Name",ComponentSerialization.CODEC.encodeStart(context.ops(),this.customNameText).getOrThrow());
        else
            json.addProperty("Name", this.customName.isBlank() ? "Trader" : this.customName);
        if(this.hasCustomIcon())
            json.add("CustomIcon",IconData.CODEC.encodeStart(context.ops(),this.getCustomIcon()).getOrThrow());
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Name"))
            this.customNameText = ComponentSerialization.CODEC.decode(context.ops(),json.get("Name")).getOrThrow(JsonSyntaxException::new).getFirst();
        if(json.has("CustomIcon"))
            this.customIcon = Optional.of(IconData.CODEC.decode(context.ops(),json.get("CustomIcon")).getOrThrow(JsonSyntaxException::new).getFirst());
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new DisplaySettings(trader,this));
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.CHANGE_NAME, 1);
    }

}
