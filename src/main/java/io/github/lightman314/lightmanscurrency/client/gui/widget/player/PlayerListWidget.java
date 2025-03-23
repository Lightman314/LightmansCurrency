package io.github.lightman314.lightmanscurrency.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerListWidget extends EasyWidgetWithChildren implements IScrollable, IEasyTickable {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/player_list.png");

    private static final int UPPER_SIZE = 40;

    private final boolean addPlayerFields;
    private final int rows;
    private final Supplier<List<PlayerEntry>> playerList;
    private final List<PlayerAction> actions;
    private final Consumer<PlayerReference> addPlayer;
    private final Function<PlayerReference,Component> addPlayerTooltip;
    private final Predicate<PlayerReference> canAddPlayer;

    @Nullable
    private EditBox playerNameField;

    @Nullable
    private final PlayerListWidget oldWidget;

    private int scroll = 0;
    private int requestDataTick = 10;

    private PlayerListWidget(@Nonnull Builder builder) {
        super(builder);
        this.addPlayerFields = builder.addPlayerFields;
        this.rows = builder.rows;
        this.playerList = builder.playerList;
        this.actions = builder.actions;
        this.addPlayer = builder.addPlayer;
        this.addPlayerTooltip = builder.addPlayerTooltip;
        this.canAddPlayer = builder.canAddPlayer;
        this.oldWidget = builder.oldWidget;
        if(this.oldWidget != null)
            this.scroll = this.oldWidget.scroll;
    }

    @Override
    public void addChildren(@Nonnull ScreenArea area) {
        int startY = 0;
        if(this.addPlayerFields)
        {
            startY = 40;
            //Edit Box at the top
            this.playerNameField = this.addChild(new EditBox(Minecraft.getInstance().font,area.x,area.y,area.width,20,this.oldWidget == null ? null : this.oldWidget.playerNameField, EasyText.empty()));
            this.playerNameField.setMaxLength(16);
            this.playerNameField.setResponder(this::onNameInputChanged);
            //Add Player Button
            this.addChild(IconButton.builder()
                    .position(area.pos.offset(area.width - 20,20))
                    .icon(IconUtil.ICON_PLUS)
                    .pressAction(this::addPlayer)
                    .addon(EasyAddonHelper.visibleCheck(() -> this.playerInTextBox() != null))
                    .addon(EasyAddonHelper.activeCheck(this::canAddPlayer))
                    .addon(EasyAddonHelper.tooltip(() -> {
                        PlayerReference player = this.playerInTextBox();
                        if(player == null)
                            return null;
                        return this.addPlayerTooltip.apply(player);
                    }))
                    .build());
        }

        //For each row, create buttons for each possible action
        for(int r = 0; r < this.rows; ++r)
        {
            for(int a = 0; a < this.actions.size(); ++a)
            {
                final PlayerAction action = this.actions.get(a);
                final int row = r;
                this.addChild(IconButton.builder()
                        .position(area.pos.offset(area.width - 20 * (a + 1),startY + r * 20))
                        .icon(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.buttonIcon(entry.player);
                            return IconData.Null();
                        })
                        .pressAction(() -> this.takeAction(row,action))
                        .addon(EasyAddonHelper.tooltip(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.tooltip(entry.player);
                            return null;
                        }))
                        .addon(EasyAddonHelper.visibleCheck(() -> this.getEntry(row) != null))
                        .addon(EasyAddonHelper.activeCheck(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.canTrigger(entry.player);
                            return false;
                        }))
                        .build());

            }
        }

        //Scroll Bar/Listener
        ScrollBarWidget.Builder sbb = ScrollBarWidget.builder()
                .position(area.pos.offset(area.width,startY))
                .height(this.height - startY)
                .scrollable(this)
                .addon(EasyAddonHelper.visibleCheck(this::isVisible));
        if(this.rows < 3)
            sbb.smallKnob();
        this.addChild(sbb.build());
        this.addChild(ScrollListener.builder().area(area).listener(this).build());

    }

    private void onNameInputChanged(String changed) { this.requestDataTick = 10; }

    @Override
    public void tick() {
        if(this.requestDataTick > 0 && this.playerNameField != null && !this.playerNameField.getValue().isBlank())
            this.requestDataTick--;
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        this.validateScroll();

        int startY = 0;
        if(this.addPlayerFields)
        {
            startY = 40;
            //Render Pending Player
            PlayerReference pendingPlayer = this.playerInTextBox();
            if(pendingPlayer != null)
            {
                gui.blitHorizSplit(GUI_TEXTURE,0,20,this.width - 20,20,0,20,180,5);
                gui.renderItem(pendingPlayer.getSkull(true),2,22);
                gui.drawShadowed(pendingPlayer.getName(true),24,26,0xFFFFFF);
            }
        }

        //Draw each additional row
        List<PlayerEntry> entries = this.getEntries();
        for(int r = 0; r < this.rows; ++r)
        {
            int index = this.scroll + r;
            boolean missingEntry = index < 0 || index >= entries.size();
            int drawWidth = missingEntry ? this.width : this.width - this.actions.size() * 20;
            //Fit background width to not overlap with the action buttons
            gui.blitHorizSplit(GUI_TEXTURE,0,startY + (20 * r),drawWidth,20,0,0,180,5);
            if(missingEntry)
                continue;
            PlayerEntry entry = entries.get(index);
            gui.renderItem(entry.player.getSkull(true),2,startY + 2 + (20 * r));
            gui.drawShadowed(entry.player.getName(true),24,startY + 6 + (20 * r),entry.color);
        }

    }

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.rows,this.getEntries().size()); }

    @Nullable
    private PlayerReference playerInTextBox() {
        if(this.playerNameField == null)
            return null;
        if(this.requestDataTick > 0)
        {
            //Manually check if player is already in the local cache
            UUID playerID = ClientPlayerNameCache.lookupID(this.playerNameField.getValue(),false);
            if(playerID != null)
                return PlayerReference.of(playerID,this.playerNameField.getValue());
            return null;
        }
        return PlayerReference.of(true,this.playerNameField.getValue());
    }

    private boolean canAddPlayer()
    {
        PlayerReference player = this.playerInTextBox();
        if(player != null)
            return this.canAddPlayer.test(player) && this.playerList.get().stream().noneMatch(e -> e.player.is(player));
        return false;
    }

    private void addPlayer()
    {
        PlayerReference player = this.playerInTextBox();
        if(player != null)
        {
            this.addPlayer.accept(player);
            this.playerNameField.setValue("");
        }
    }

    @Nonnull
    private List<PlayerEntry> getEntries() { return this.playerList.get(); }

    @Nullable
    private PlayerEntry getEntry(int row)
    {
        List<PlayerEntry> entries = this.getEntries();
        int index = row + this.scroll;
        if(index < 0 || index >= entries.size())
            return null;
        return entries.get(index);
    }

    private void takeAction(int row,PlayerAction action)
    {
        PlayerEntry entry = this.getEntry(row);
        if(entry != null)
            action.onTrigger(entry.player);
    }

    private boolean showAction(int player)
    {
        List<PlayerEntry> entries = this.getEntries();
        return player + this.scroll < entries.size();
    }

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {

        private Builder() { super(100,UPPER_SIZE + 20); }
        @Override
        protected Builder getSelf() { return this; }

        private boolean addPlayerFields = true;
        private int rows = 1;
        private Supplier<List<PlayerEntry>> playerList = ArrayList::new;
        private final List<PlayerAction> actions = new ArrayList<>();
        private Consumer<PlayerReference> addPlayer = p -> {};
        private Function<PlayerReference,Component> addPlayerTooltip = p -> LCText.BUTTON_ADD.get();
        private Predicate<PlayerReference> canAddPlayer = p -> true;
        @Nullable
        private PlayerListWidget oldWidget = null;

        public Builder dontAddPlayers() { this.addPlayerFields = false; this.changeHeight(this.rows * 20); return this; }
        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder rows(int rows) { this.rows = rows; this.changeHeight(UPPER_SIZE + (20 * this.rows)); return this; }
        public Builder playerList(Supplier<List<PlayerReference>> playerSource) { this.playerList = () -> playerSource.get().stream().map(PlayerEntry::of).toList(); return this; }
        public Builder entryList(Supplier<List<PlayerEntry>> playerList) { this.playerList = playerList; return this; }
        public Builder action(PlayerAction action) { this.actions.add(action); return this; }
        public Builder addPlayer(Consumer<PlayerReference> addPlayer) { this.addPlayer = addPlayer; return this; }
        public Builder addPlayerTooltip(TextEntry text) { this.addPlayerTooltip = p -> text.get(); return this; }
        public Builder addPlayerTooltip(Component text) { this.addPlayerTooltip = p -> text; return this; }
        public Builder addPlayerTooltip(Supplier<Component> text) { this.addPlayerTooltip = p -> text.get(); return this; }
        public Builder addPlayerTooltip(Function<PlayerReference,Component> text) { this.addPlayerTooltip = text; return this; }
        public Builder canAddPlayer(Supplier<Boolean> canAddPlayer) { this.canAddPlayer = p -> canAddPlayer.get(); return this; }
        public Builder canAddPlayer(Predicate<PlayerReference> canAddPlayer) { this.canAddPlayer = canAddPlayer; return this; }
        public Builder oldWidget(PlayerListWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public PlayerListWidget build() { return new PlayerListWidget(this); }

    }

}
