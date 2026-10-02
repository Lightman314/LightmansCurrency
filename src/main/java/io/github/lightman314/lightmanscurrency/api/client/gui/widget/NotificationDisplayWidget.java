package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationStack;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NotificationDisplayWidget extends AbstractMultiWidget.LateChildren implements IScrollable {

    public static final Identifier BG = LCApi.id("widget/notification/background");
    public static final Identifier BG_UNSEEN = LCApi.id("widget/notification/background_unseen");
    public static final Identifier DIVIDER = LCApi.id("widget/notification/divider");
    public static final Identifier DIVIDER_UNSEEN = LCApi.id("widget/notification/unseen");
    public static final Pair<Identifier,Identifier> SPRITES = Pair.of(BG,DIVIDER);
    public static final Pair<Identifier,Identifier> UNSEEN_SPRITES = Pair.of(BG_UNSEEN,DIVIDER_UNSEEN);

    public static final int HEIGHT_PER_ROW = 22;

    private final BooleanSupplier showGeneralMessage;
    private final Supplier<List<NotificationStack>> source;
    private final int rows;
    private final boolean colorIfUnseen;
    private final Consumer<Integer> deletionHandler;
    private final BooleanSupplier canDelete;

    private int scroll = 0;

    private List<NotificationStack> notificationCache = new ArrayList<>();

    public NotificationDisplayWidget(Builder builder) {
        super(builder);
        this.rows = builder.rows;
        this.source = builder.source;
        this.showGeneralMessage = builder.showGeneral;
        this.colorIfUnseen = builder.colorIfUnseen;
        this.deletionHandler = builder.deleteHandler;
        this.canDelete = builder.canDelete;
        if(builder.oldWidget != null)
            this.scroll = builder.oldWidget.scroll;
    }

    @Override
    protected void addLateChildren(ScreenArea area) {
        for(int i = 0; i < this.rows; ++i) {
            final int row = i;
            this.addChild(IconButton.builder()
                    .atPos(area.pos.offset(this.width - 21,1 + (i * HEIGHT_PER_ROW)))
                    .onPress(() -> this.deleteNotification(row))
                    .withIcon(SpriteIcon.of(LCApi.id("icon/sign_x")))
                    .visible(this.deleteButtonVisible(row))
                    .tooltip(TooltipSource.simple(Notification.TOOLTIP_DELETE))
                    .build());
        }
        //Add Scroll Bar
        this.addChild(VerticalScrollBar.builder(this)
                .rightOf(this)
                .visible(this::isVisible)
                .build());
        //And a scroll area
        this.addChild(ScrollArea.builder()
                .ofArea(area)
                .withListener(this.buildScrollListener())
                .active(this::isVisible)
                .build());

    }

    private BooleanSupplier deleteButtonVisible(int row) {
        return () -> {
            if(this.isVisible() && this.canDelete.getAsBoolean())
                return this.notificationCache.size() > row;
            return false;
        };
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        //Cache the notifications so that we don't have to re-obtain them multiple times per frame
        this.notificationCache = this.source.get();
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        this.validateScroll();

        boolean showGeneral = this.showGeneralMessage.getAsBoolean();

        List<Component> tooltip = null;

        int index = this.scroll;

        boolean deletingEnabled = this.canDelete.getAsBoolean();

        for(int y = 0; y < this.rows; ++y) {

            int yPos = y * HEIGHT_PER_ROW;
            NotificationStack n = null;
            if(index < this.notificationCache.size())
                n = this.notificationCache.get(index++);

            //Draw the BG
            Pair<Identifier,Identifier> sprites = this.colorIfUnseen && (n != null && !n.wasSeen()) ? UNSEEN_SPRITES : SPRITES;
            gui.blitSprite(sprites.first(),0,yPos,this.width,HEIGHT_PER_ROW);

            if(n != null) {
                //Draw the text
                int textXPos = 2;
                int textWidth = this.width - 4;
                if(deletingEnabled) //Shrink the text width by 20 if the delete button will be visible
                    textWidth -= 20;
                //Text is now black in all states
                int textColor = ARGB.opaque(0);
                int centerY = yPos + ((HEIGHT_PER_ROW - gui.getFont().lineHeight) / 2);
                if(n.getCount() > 1) {
                    //Render quantity text
                    String countText = NumberHelper.prettyInteger(n.getCount());
                    int quantityWidth = gui.getFont().width(countText);
                    //Seperator
                    gui.blitSprite(sprites.second(),1 + quantityWidth,yPos,3,HEIGHT_PER_ROW);

                    //Count Text
                    gui.text(countText,textXPos,centerY,textColor,false);

                    //Modify the rest of the text space
                    textXPos += quantityWidth + 2;
                    textWidth -= quantityWidth + 2;
                }

                List<Component> messages = showGeneral ? n.getGeneralMessage() : n.getMessageLines();
                //Draw the lines
                List<FormattedCharSequence> lines = new ArrayList<>();
                for(Component line : messages)
                    lines.addAll(gui.getFont().split(line,textWidth));
                if(lines.size() == 1)
                    gui.text(lines.getFirst(),textXPos,centerY,textColor,false);
                else {
                    for(int i = 0; i < lines.size() && i < 2; ++i)
                        gui.text(lines.get(i),textXPos,yPos + 2 + (i * 10),textColor,false);
                }
                int maxX = this.getX() + this.width;
                if(deletingEnabled)
                    maxX -= 22;
                //Collect the tooltips
                if(tooltip == null && gui.getMousePos().x >= this.getX() && gui.getMousePos().x < maxX && gui.getMousePos().y >= this.getY() + yPos && gui.getMousePos().y < this.getY() + yPos + HEIGHT_PER_ROW) {
                    tooltip = new ArrayList<>();
                    tooltip.add(n.getTimeStampMessage());
                    if(lines.size() > 2)
                        tooltip.addAll(TooltipHelper.splitTooltips(messages));
                }
            }
        }
        //Render the tooltips
        if(tooltip != null && !tooltip.isEmpty())
            gui.renderTooltipAtMouse(tooltip);
    }

    @Override
    protected List<Component> collectTooltips(ScreenPosition mousePos) { return null; }

    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.notificationCache.size(),this.rows); }

    private void deleteNotification(int row) { this.deletionHandler.accept(row + this.scroll); }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBuilder<Builder,NotificationDisplayWidget> {

        private Builder() { super(100,HEIGHT_PER_ROW); }

        @Override
        protected Builder getSelf() { return this; }

        @Nullable
        private NotificationDisplayWidget oldWidget = null;

        private int rows = 1;
        private Supplier<List<NotificationStack>> source = List::of;
        private BooleanSupplier showGeneral = () -> false;
        private boolean colorIfUnseen = false;
        private Consumer<Integer> deleteHandler = i -> {};
        private BooleanSupplier canDelete = () -> false;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }
        public Builder withRows(int rows) { this.rows = rows; this.setHeight(this.rows * HEIGHT_PER_ROW); return this; }

        public Builder withOldWidget(@Nullable NotificationDisplayWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder withNotifications(Supplier<List<NotificationStack>> source) { this.source = source; return this; }

        public Builder showGeneral(boolean showGeneral) { return this.showGeneral(() -> showGeneral); }
        public Builder showGeneral(BooleanSupplier showGeneral) { this.showGeneral = showGeneral; return this; }

        public Builder colorIfUnseen() { this.colorIfUnseen = true; return this; }

        public Builder deletionHandler(Consumer<Integer> handler) { return this.deletionHandler(handler,() -> true); }
        public Builder deletionHandler(Consumer<Integer> handler,BooleanSupplier canDelete) {
            this.deleteHandler = handler;
            this.canDelete = canDelete;
            return this;
        }

        @Override
        public NotificationDisplayWidget build() { return new NotificationDisplayWidget(this); }

    }

}
