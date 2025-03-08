package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.common.menus.gacha_machine.GachaMachineMenu;
import io.github.lightman314.lightmanscurrency.common.traders.gatcha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenNetworkTerminal;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenStorage;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GachaMachineScreen extends EasyMenuScreen<GachaMachineMenu> {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/gacha_machine.png");
    public static final ResourceLocation OVERLAY_TEXTURE = VersionUtil.lcResource("textures/gui/container/gacha_machine_overlay.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public static final int ANIMATION_LENGTH = 20;
    public static final int REST_LENGTH = 8;

    public static final int BALL_SIZE = 9;
    public static final int BALL_RENDER_LEFT = 44;
    public static final int BALL_RENDER_BOTTOM = 59;
    public static final int BALL_RENDER_COUNT_X = 9;
    public static final int BALL_RENDER_COUNT_Y = 7;

    public static final ScreenPosition REWARD_END_POSITION = ScreenPosition.of(80,87);

    public static final ScreenPosition KNOB_POSITION = ScreenPosition.of(81,73);
    public static final int KNOB_SIZE = 14;

    public static final Sprite SPRITE_INTERACT = Sprite.SimpleSprite(GUI_TEXTURE, WIDTH + 2 * KNOB_SIZE, 0, 18, 18);

    private int animationTick = 0;
    private int restTick = 0;

    private int tradeMultiplier = 1;

    public int getMachineColor() {
        GachaTrader trader = this.menu.getTrader();
        return trader == null ? 0xFFFFFF : trader.getColor();
    }

    EasyButton buttonInteract;

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;

    IconButton buttonOpenTerminal;

    public final LazyWidgetPositioner rightEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.createTopdown(), WIDTH, 0, 20);

    private static final ScreenPosition INFO_WIDGET_POSITION = ScreenPosition.of(160, HEIGHT - 96);

    private static final ScreenArea GACHA_INFO_AREA = ScreenArea.of(160,9,11,11);

    public GachaMachineScreen(GachaMachineMenu menu, Inventory inventory, Component title) {
        super(menu,inventory,title);
        this.resize(WIDTH,HEIGHT);
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.addChild(this.rightEdgePositioner);
        this.buttonOpenStorage = this.addChild(IconButton.builder()
                .pressAction(this::OpenStorage)
                .icon(IconUtil.ICON_STORAGE)
                .addon(EasyAddonHelper.visibleCheck(() -> this.menu.getTrader() != null && this.menu.getTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_OPEN_STORAGE))
                .build());
        this.buttonCollectCoins = this.addChild(IconAndButtonUtil.finishCollectCoinButton(IconButton.builder().pressAction(this::CollectCoins), this.menu.player, this.menu::getTrader));
        this.buttonOpenTerminal = this.addChild(IconButton.builder()
                .pressAction(this::OpenTerminal)
                .icon(IconUtil.ICON_BACK)
                .addon(EasyAddonHelper.visibleCheck(this::showTerminalButton))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_NETWORK_BACK))
                .build());

        this.rightEdgePositioner.clear();
        this.rightEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);
        this.addChild(this.rightEdgePositioner);

        this.buttonInteract = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(7,107))
                .pressAction(this::ExecuteTrades)
                .sprite(SPRITE_INTERACT)
                .addon(EasyAddonHelper.tooltips(this::getInteractionTooltip))
                .addon(EasyAddonHelper.activeCheck(this::allowInteraction))
                .build());

        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(29,105))
                .pressAction(this::increaseTradeMult)
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .addon(EasyAddonHelper.activeCheck(this::canIncreaseTradeMult))
                .build());

        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(29,115))
                .pressAction(this::decreaseTradeMult)
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .addon(EasyAddonHelper.activeCheck(this::canDecreaseTradeMult))
                .build());

    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.resetColor();

        //Main BG
        gui.renderNormalBackground(GUI_TEXTURE,this);

        //Coin info widget
        gui.blit(TraderScreen.GUI_TEXTURE, INFO_WIDGET_POSITION, 244, 0, 10, 10);

        //Gacha info widget
        gui.blit(GUI_TEXTURE,GACHA_INFO_AREA.pos,WIDTH + 2 * KNOB_SIZE,36,11,11);

        //Render Gacha Balls in the machine itself
        GachaTrader trader = this.menu.getTrader();
        if(trader != null)
        {
            //Create random source from the blocks position and the traders id
            //This way the randomization will be consistent whenever the player opens/closes the same traders menu
            //But will be different for different machines
            RandomSource random = RandomSource.create(Objects.hash(trader.getWorldPosition(),trader.getID()));

            List<ItemStack> contents = trader.getStorage().peekRandomItems(random,BALL_RENDER_COUNT_X * BALL_RENDER_COUNT_Y);

            for(int y = 0; y < BALL_RENDER_COUNT_Y && !contents.isEmpty(); ++y)
            {
                int xOffset = 0;
                if(contents.size() < BALL_RENDER_COUNT_X)
                    xOffset = Math.round(((float)(BALL_RENDER_COUNT_X - contents.size()) * (float)BALL_SIZE)/2f);
                for(int x = 0; x < BALL_RENDER_COUNT_X && !contents.isEmpty(); ++x)
                {
                    //Render the item
                    gui.renderItem(GachaBallItem.createWithItem(contents.removeFirst(),random),BALL_RENDER_LEFT + xOffset + (x * BALL_SIZE),BALL_RENDER_BOTTOM - (y * BALL_SIZE),"");
                }
            }

            if(this.menu.hasPendingReward() && this.animationTick >= ANIMATION_LENGTH - 8 && this.animationTick < ANIMATION_LENGTH)
            {
                //Render reward item falling
                int distance = (ANIMATION_LENGTH - this.animationTick - 1) * 2;
                if(gui.partialTicks >= 0.5f)
                    distance--;
                gui.renderItem(this.menu.getNextReward(),REWARD_END_POSITION.offset(0,distance * -1));
            }

        }

        //Render trade count
        gui.drawString(LCText.GUI_GACHA_MACHINE_TRADE_MULTIPLIER.get(this.tradeMultiplier), 40,113,0x404040);

        //Render colored overlays
        gui.pushPose().TranslateToForeground();
        final int machineColor = this.getMachineColor();

        //Render overlay in the same color as the machine itself
        gui.renderNormalBackground(OVERLAY_TEXTURE,this,machineColor);
        //Render Knob
        ScreenPosition knobUV = this.getKnobUV(this.animationTick,gui.partialTicks);
        gui.blit(GUI_TEXTURE,KNOB_POSITION,knobUV.x,knobUV.y,KNOB_SIZE,KNOB_SIZE);
        gui.setColor(machineColor,1f);
        gui.blit(OVERLAY_TEXTURE,KNOB_POSITION,knobUV.x,knobUV.y,KNOB_SIZE,KNOB_SIZE);

        //Reset
        gui.popPose();
        gui.resetColor();

    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        gui.pushPose().TranslateToForeground();
        if(INFO_WIDGET_POSITION.offset(this).isMouseInArea(gui.mousePos, 10, 10))
            gui.renderComponentTooltip(this.menu.getContext().getAvailableFundsDescription());
        if(GACHA_INFO_AREA.offsetPosition(this.getCorner()).isMouseInArea(gui.mousePos))
            gui.renderComponentTooltip(this.getGachaInfoTooltip());
    }

    @Override
    protected void renderAfterTooltips(@Nonnull EasyGuiGraphics gui) {
        gui.popPose();
    }

    private ScreenPosition getKnobUV(final int animationTick, float partialTick)
    {
        int u = WIDTH;

        int frame = (animationTick * 2) - 1;
        if(partialTick >= 0.5f)
            frame += 1;
        //If frame is greater than 40, assume default knob position
        if(animationTick >= 20)
            frame = 0;
        //Frame 0 -> 20 repeat twice for full knob rotation
        else if(frame >= 20)
            frame -= 20;
        if(frame >= 10)
        {
            u += KNOB_SIZE;
            frame -= 10;
        }
        return ScreenPosition.of(u,KNOB_SIZE * Math.clamp(frame,0,9));
    }

    @Override
    protected void screenTick() {
        if(this.menu.hasPendingReward() && this.animationTick == 0)
        {
            this.animationTick++;
        }
        else if(this.animationTick > 0)
        {
            if(this.animationTick >= ANIMATION_LENGTH)
            {
                this.restTick++;
                if(this.restTick >= REST_LENGTH)
                {
                    this.animationTick = 0;
                    this.restTick = 0;
                    //Send message if this is the last reward known to the client to let the server know that we think we're done
                    //Shouldn't be strictly needed, but it's here as a failsafe
                    if(!this.menu.hasPendingReward())
                        this.menu.SendMessageToServer(this.builder().setFlag("AnimationsCompleted"));
                }
            }
            else
            {
                this.animationTick++;
                //Send reward flag
                if(this.animationTick >= ANIMATION_LENGTH)
                {
                    this.menu.getAndRemoveNextReward();
                    this.menu.SendMessageToServer(this.builder().setFlag("GiveNextReward"));
                    //Play Sound
                    this.playDing();
                }
            }
        }
    }

    private boolean canIncreaseTradeMult() { return this.tradeMultiplier < 10; }

    private void increaseTradeMult()
    {
        if(this.tradeMultiplier <= 1)
            this.tradeMultiplier = 5;
        else if(this.tradeMultiplier <= 5)
            this.tradeMultiplier = 10;
    }

    private boolean canDecreaseTradeMult() { return this.tradeMultiplier > 1; }

    private void decreaseTradeMult()
    {
        if(this.tradeMultiplier >= 10)
            this.tradeMultiplier = 5;
        else if(this.tradeMultiplier >= 5)
            this.tradeMultiplier = 1;
    }

    private boolean allowInteraction() {
        GachaTrader trader = this.menu.getTrader();
        return trader != null && !this.menu.hasPendingReward() && !trader.getStorage().isEmpty() && trader.getPrice().isValidPrice();
    }

    private boolean showTerminalButton() {
        if(this.menu.getTrader() != null)
            return this.menu.getTrader().showOnTerminal();
        return false;
    }

    private void ExecuteTrades() {
        this.menu.SendMessageToServer(this.builder().setInt("ExecuteTrade",this.tradeMultiplier));
    }

    private void OpenStorage(EasyButton button) {
        if(this.menu.getTrader() != null)
            new CPacketOpenStorage(this.menu.getTrader().getID()).send();
    }

    private void CollectCoins(EasyButton button) {
        if(this.menu.getTrader() != null)
            CPacketCollectCoins.sendToServer();
    }

    private void OpenTerminal(EasyButton button) {
        if(this.showTerminalButton())
            new CPacketOpenNetworkTerminal().send();
    }

    private List<Component> getInteractionTooltip() {
        GachaTrader trader = this.menu.getTrader();
        if(trader != null)
        {
            MoneyValue normalCost = trader.getPrice();
            MoneyValue currentCost = trader.runTradeCostEvent(trader.getTrade(0), this.menu.getContext()).getCostResult();
            Component costText = currentCost.isFree() ? LCText.TOOLTIP_SLOT_MACHINE_COST_FREE.get() : currentCost.getText();
            List<Component> result;
            if(this.tradeMultiplier == 1)
                result = LCText.TOOLTIP_GACHA_MACHINE_ROLL_ONCE.get(this.tradeMultiplier,costText);
            else
                result = LCText.TOOLTIP_GACHA_MACHINE_ROLL_MULTI.get(this.tradeMultiplier,costText);
            //If the price is modified by a trade rule, display the "normal cost" as well just in case it changes in-between rolls
            if(!currentCost.equals(normalCost) && this.tradeMultiplier > 1)
                result.add(LCText.TOOLTIP_GACHA_MACHINE_NORMAL_COST.get(normalCost.isFree() ? LCText.TOOLTIP_GACHA_MACHINE_COST_FREE.get() : normalCost.getText()));
            return result;
        }
        return ImmutableList.of();
    }

    private List<Component> getGachaInfoTooltip() {
        List<Component> list = new ArrayList<>();
        GachaTrader trader = this.menu.getTrader();
        if(trader == null || !trader.getPrice().isValidPrice())
        {
            list.add(LCText.TOOLTIP_GACHA_MACHINE_UNDEFINED.get());
            return list;
        }
        if(trader.getStorage().isEmpty())
        {
            list.add(LCText.TOOLTIP_GACHA_MACHINE_EMPTY.get());
            return list;
        }
        list.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS_LABEL.get());
        for(ItemStack item : trader.getStorage().getContents())
            list.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS.get(item.getCount(),item.getHoverName()));
        return list;
    }

    private void playDing()
    {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if(soundManager != null)
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f));
    }

}
