package io.github.lightman314.lightmanscurrency.api.client.gui.widget.player;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.*;

public class PlayerListWidget extends AbstractMultiWidget.LateChildren implements IScrollable, ITickerClient {

    private static final int UPPER_SIZE = 40;

    public static final Identifier PLAYER_DISPLAY = LCApi.id("widget/player_display");

    private final boolean addPlayerFields;
    private final int rows;
    private final Supplier<List<PlayerEntry>> playerList;
    private final List<PlayerAction> actions;
    private final Consumer<PlayerReference> addPlayer;
    private final Function<PlayerReference,Component> addPlayerTooltip;
    private final Predicate<PlayerReference> canAddPlayer;

    @Nullable
    private TextBoxWrapper<String> playerNameField;
    private List<PlayerEntry> playerEntryCache = List.of();

    private int scroll = 0;
    private int requestDataTick = 10;

    protected PlayerListWidget(Builder builder) {
        super(builder);
        this.addPlayerFields = builder.addPlayerFields;
        this.rows = builder.rows;
        this.playerList = builder.playerList;
        this.actions = builder.actions;
        this.addPlayer = builder.addPlayer;
        this.addPlayerTooltip = builder.addPlayerTooltip;
        this.canAddPlayer = builder.canAddPlayer;
        if(builder.oldWidget != null) {
            this.playerNameField = builder.oldWidget.playerNameField;
            this.scroll = builder.oldWidget.scroll;
        }
    }

    @Override
    protected void addLateChildren(ScreenArea area) {
        int startY = 0;
        if(this.addPlayerFields) {
            startY = UPPER_SIZE;
            //Text Box at the tope
            this.playerNameField = this.addChild(TextBoxWrapper.stringBuilder()
                    .atPos(area.pos)
                    .ofWidth(area.width)
                    .withOldWidget(this.playerNameField)
                    .withMaxLength(16)
                    .withHandler(this::onNameInputChanged)
                    .build());
            //Add Player Button
            this.addChild(IconButton.builder()
                    .atPos(area.pos.offset(area.width - 20,20))
                    .withIcon(SpriteIcon.of(LCApi.id("icon/sign_plus")))
                    .onPress(this::addPlayer)
                    .visible(() -> this.playerInTextBox() != null)
                    .active(this::canAddPlayer)
                    .tooltip(TooltipSource.deferredSingle(() -> {
                        PlayerReference player = this.playerInTextBox();
                        if(player == null)
                            return null;
                        return this.addPlayerTooltip.apply(player);
                    }))
                    .build());
        }

        //For each row, create buttons for each possible action
        for(int r = 0; r < this.rows; ++r) {
            for(int a = 0; a < this.actions.size(); ++a) {
                final PlayerAction action = this.actions.get(a);
                final int row = r;
                this.addChild(IconButton.builder()
                        .atPos(area.pos.offset(area.width - 20 * (a + 1),startY + r * 20))
                        .withIcon(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.buttonIcon(entry.player());
                            return IconData.empty();
                        })
                        .onPress(() -> this.takeAction(row,action))
                        .tooltip(TooltipSource.deferredSingle(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.tooltip(entry.player());
                            return null;
                        }))
                        .active(() -> {
                            PlayerEntry entry = this.getEntry(row);
                            if(entry != null)
                                return action.canTrigger(entry.player());
                            return false;
                        })
                        .visible(() -> this.getEntry(row) != null)
                        .build());
            }
        }

        //Scroll Bar/Listener
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width,startY))
                .ofHeight(this.height - startY)
                .visible(this::isVisible)
                .withKnob(this.rows < 3 ? VerticalScrollBar.SMALL_KNOB : VerticalScrollBar.DEFAULT_KNOB)
                .build());
        this.addChild(ScrollArea.builder()
                .ofArea(area)
                .withListener(this.buildScrollListener())
                .build());

    }

    private void onNameInputChanged(String changed) { this.requestDataTick = 10; }

    @Override
    public void clientTick() {
        if(this.requestDataTick > 0 && this.playerNameField != null && !this.playerNameField.getString().isBlank())
            this.requestDataTick--;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        this.playerEntryCache = this.playerList.get();
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        this.validateScroll();

        int startY = 0;
        if(this.addPlayerFields) {
            startY = 40;
            //Render Pending Player
            PlayerReference pendingPlayer = this.playerInTextBox();
            if(pendingPlayer != null) {
                gui.blitSprite(PLAYER_DISPLAY,0,20,this.width - 20,20);
                gui.item(pendingPlayer.getSkull().create(),2,22);
                gui.text(pendingPlayer.getName(ISidedContext.LOGICAL_CLIENT),24,26,-1,true);
            }
        }
        //Draw each additional row
        for(int r = 0; r < this.rows; ++r) {
            int index = this.scroll + r;
            int yPos = startY + 20 * r;
            PlayerEntry entry = this.getEntry(r);
            int drawWidth = entry == null ? this.width : this.width - this.actions.size() * 20;
            //Fit background width to not overlap with the action buttons
            gui.blitSprite(LCSprites.BUTTON_BROWN.get(true,false),0,yPos,drawWidth,20);
            if(entry == null)
                continue;
            gui.item(entry.getSkull(),2,yPos + 2);
            gui.text(entry.getName(),24,yPos + 6,entry.color(),false);
        }
    }

    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.playerEntryCache.size(),this.rows); }

    @Nullable
    private PlayerReference playerInTextBox() {
        if(this.playerNameField == null)
            return null;
        if(this.requestDataTick > 0) {
            //Manually check if the player is already in the local cache
            UUID playerID = ClientPlayerNameCache.lookupID(this.playerNameField.getString(),false);
            if(playerID != null)
                return PlayerReference.of(playerID,this.playerNameField.getString());
            return null;
        }
        return PlayerReference.of(ISidedContext.LOGICAL_CLIENT,this.playerNameField.getString());
    }

    private boolean canAddPlayer() {
        PlayerReference player = this.playerInTextBox();
        if(player != null)
            return this.canAddPlayer.test(player) && this.playerEntryCache.stream().noneMatch(e -> e.player().is(player));
        return false;
    }

    private void addPlayer() {
        PlayerReference player = this.playerInTextBox();
        if(player != null) {
            this.addPlayer.accept(player);
            this.playerNameField.setStringValue("");
        }
    }

    @Nullable
    private PlayerEntry getEntry(int row) { return ListHelper.getOrNull(this.playerEntryCache,row + this.scroll); }

    private void takeAction(int row,PlayerAction action) {
        PlayerEntry entry = this.getEntry(row);
        if(entry != null)
            action.onTrigger(entry.player());
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,PlayerListWidget> {

        private Builder() { super(100,UPPER_SIZE + 20); }

        private boolean addPlayerFields = true;
        private int rows = 1;
        private Supplier<List<PlayerEntry>> playerList = List::of;
        private final List<PlayerAction> actions = new ArrayList<>();
        private Consumer<PlayerReference> addPlayer = p -> {};
        private Function<PlayerReference, Component> addPlayerTooltip = p -> PlayerReference.TOOLTIP_ADD_PLAYER.get();
        private Predicate<PlayerReference> canAddPlayer = p -> true;
        @Nullable
        PlayerListWidget oldWidget = null;

        @Override
        protected Builder getSelf() { return this; }

        public Builder dontAddPlayers() { this.addPlayerFields = false; this.setHeight(this.rows * 20); return this; }
        public Builder ofWidth(int width) { this.setWidth(width); return this; }
        public Builder withRows(int rows) { this.rows = rows; this.setHeight((this.addPlayerFields ? UPPER_SIZE : 0) + this.rows * 20); return this; }

        public Builder playerList(Supplier<List<PlayerReference>> playerSource) { return this.entryList(() -> playerSource.get().stream().map(PlayerEntry::new).toList()); }
        public Builder entryList(Supplier<List<PlayerEntry>> playerList) { this.playerList = playerList; return this; }

        public Builder action(PlayerAction action) { this.actions.add(action); return this; }

        public Builder addPlayer(Consumer<PlayerReference> addPlayer) { this.addPlayer = addPlayer; return this; }

        public Builder addPlayerTooltip(Component tooltip) { return this.addPlayerTooltip(p -> tooltip); }
        public Builder addPlayerTooltip(TextEntry tooltip) { return this.addPlayerTooltip(p -> tooltip.get()); }
        public Builder addPlayerTooltip(Supplier<Component> tooltip) { return this.addPlayerTooltip(p -> tooltip.get()); }
        public Builder addPlayerTooltip(Function<PlayerReference,Component> tooltip) { this.addPlayerTooltip = tooltip; return this; }

        public Builder canAddPlayer(BooleanSupplier canAddPlayer) { return this.canAddPlayer(p -> canAddPlayer.getAsBoolean()); }
        public Builder canAddPlayer(Predicate<PlayerReference> canAddPlayer) { this.canAddPlayer = canAddPlayer; return this; }

        public Builder withOldWidget(@Nullable PlayerListWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        @Override
        public PlayerListWidget build() { return new PlayerListWidget(this); }

    }

}