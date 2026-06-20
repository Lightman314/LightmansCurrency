package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class DisplayNode extends SimpleSyncedNode implements IPersistentNode, ISettingsMessageListener, IPermissionUser {

    public static final int MAX_NAME_LENGTH = 32;

    private static final MapCodec<DisplayNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.either(Codec.STRING,ComponentSerialization.CODEC).optionalFieldOf("customName").forGetter(DisplayNode::getCustomNameEither),
            //IconData.CODEC.optionalFieldOf("customIcon").forGetter(n -> n.customIcon),
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
        return Component.literal(this.customName);
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
            //if(admin != null)
                //this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NAME.get(),this.customName));
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
            //if(admin != null)
             //   this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_NAME.get(),this.customNameText));
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

    /*private Optional<IconData> customIcon = Optional.empty();
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
        //if(admin != null)
        //    this.pushLocalNotification(ChangeSettingNotification.dumb(admin,LCText.DATA_ENTRY_TRADER_ICON.get()));
        this.setChanged(builder -> {
            builder.setBoolean("customIcon",this.customIcon.isPresent());
            this.customIcon.ifPresent(icon -> builder.setCustom("icon",icon,ModLazyPackets.ICON));
        });
        return true;
    }*/

    private boolean alwaysShowSearchBox = false;
    public final boolean alwaysShowSearchBox() { return this.alwaysShowSearchBox; }
    public final boolean setAlwaysShowSearchBox(@Nullable PlayerReference admin, boolean newVal)
    {
        if(this.alwaysShowSearchBox != newVal)
        {
            this.alwaysShowSearchBox = newVal;
            //if(admin != null)
            //    this.pushLocalNotification(ChangeSettingNotification.simple(admin,LCText.DATA_ENTRY_TRADER_ALWAYS_SHOW_SEARCH_BOX.get(),this.alwaysShowSearchBox));
            this.setChanged(builder -> builder.setBoolean("showSearchBox",this.alwaysShowSearchBox));
            return true;
        }
        return false;
    }

    private DisplayNode() {}
    private DisplayNode(Optional<Either<String,Component>> customName/*,Optional<IconData> customIcon*/,boolean alwaysShowSearchBox) {
        customName.ifPresent(either ->
                either.ifLeft(n -> this.customName = n)
                        .ifRight(c -> this.customNameText = c));
        //this.customIcon = customIcon;
        this.alwaysShowSearchBox = alwaysShowSearchBox;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        if(this.customNameText != null)
            builder.setText("customNameText",this.customNameText);
        else
            builder.setString("customName",this.customName);
        //builder.setBoolean("customIcon",this.customIcon.isPresent());
        //this.customIcon.ifPresent(icon -> builder.setCustom("icon",icon,ModLazyPackets.ICON));
        builder.setBoolean("showSearchBox",this.alwaysShowSearchBox);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
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
        /*if(data.contains("customIcon"))
        {
            if(data.getBoolean("customIcon"))
                this.customIcon = Optional.ofNullable(data.getCustom("icon",ModLazyPackets.ICON));
            else
                this.customIcon = Optional.empty();
        }//*/
        if(data.contains("showSearchBox"))
            this.alwaysShowSearchBox = data.getBoolean("showSearchBox");
    }

    @Override
    public void handleSettingsChange(Player player,FancyPacketMap message) {
        if(message.contains("ChangeName"))
        {
            if(this.getPermission(player,BuiltInPermissions.EDIT_DISPLAY))
            {
                String newName = message.getString("ChangeName");
                if(newName.length() > MAX_NAME_LENGTH)
                    newName = newName.substring(0,MAX_NAME_LENGTH);
                this.setCustomName(PlayerReference.of(player),newName);
            }
        }
        /*if(message.contains("ChangeIcon"))
        {
            if(this.hasPermission(player,Permissions.CHANGE_NAME))
            {
                IconData newIcon = message.getCustom("ChangeIcon",ModLazyPackets.ICON);
                this.setCustomIcon(PlayerReference.of(player),newIcon);
            }
        }*/
        if(message.contains("AlwaysShowSearchBox"))
        {
            if(this.getPermission(player,BuiltInPermissions.EDIT_DISPLAY))
            {
                boolean newVal = message.getBoolean("AlwaysShowSearchBox");
                this.setAlwaysShowSearchBox(PlayerReference.of(player),newVal);
            }
        }
    }

    @Override
    public void writePersistentData(ValueOutput output, String id, String ownerName) {
        Component text = this.customNameText == null ? Component.literal(this.customName.isBlank() ? "Trader" : this.customName) : this.customNameText;
        output.store("name",ComponentSerialization.CODEC,text);
        //if(this.hasCustomIcon())
        //    json.add("CustomIcon",IconData.CODEC.encodeStart(context.ops(),this.getCustomIcon()).getOrThrow());
    }

    @Override
    public void loadPersistentData(ValueInput input) {
        Optional<Component> name = input.read("name",ComponentSerialization.CODEC);
        name.ifPresentOrElse(n -> this.customNameText = n,
                () -> this.customNameText = Component.literal("Trader"));
        //if(json.has("CustomIcon"))
        //    this.customIcon = Optional.of(IconData.CODEC.decode(context.ops(),json.get("CustomIcon")).getOrThrow(JsonSyntaxException::new).getFirst());
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(BuiltInPermissions.EDIT_DISPLAY);
    }

    /*@Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new DisplaySettings(trader,this));
    }*/

}