package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.SlotMachineScreen;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.menus.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class SlotMachineRenderer implements IEasyTickable {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/slot_machine_overlay.png");

    public Font getFont() { return this.screen.getFont(); }
    private final SlotMachineScreen screen;
    private final SlotMachineMenu menu;
    private SlotMachineTraderData getTrader() { return this.menu.getTrader(); }
    private RandomSource getRandom() { return this.menu.player.level.random; }

    public static int GetAnimationTime() { return Math.max(LCConfig.CLIENT.slotMachineAnimationTime.get(), 20); }
    public static int GetRestTime() { return Math.max(LCConfig.CLIENT.slotMachineAnimationRestTime.get(), 1); }

    private int animationTick = 0;
    private int restTick = 0;

    public SlotMachineRenderer(SlotMachineScreen screen) { this.screen = screen; this.menu = this.screen.getMenu(); this.recollectPossibleBlocks(); this.initializeLines(); }

    public final NonNullList<SlotMachineLine> lines = NonNullList.create();

    private final List<SlotMachineRenderBlock> possibleBlocks = new ArrayList<>();
    private int totalWeight = 0;

    public SlotMachineRenderBlock getRandomBlock()
    {
        if(this.totalWeight <= 0)
            return SlotMachineRenderBlock.empty();
        int rand = this.getRandom().nextInt(this.totalWeight) + 1;
        for(SlotMachineRenderBlock block : this.possibleBlocks)
        {
            rand -= block.weight;
            if(rand <= 0)
                return block;
        }
        return SlotMachineRenderBlock.empty();
    }

    private void recollectPossibleBlocks()
    {
        SlotMachineTraderData trader = this.getTrader();
        this.possibleBlocks.clear();
        this.totalWeight = 0;
        if(trader != null)
        {
            for(SlotMachineEntry entry : trader.getValidEntries())
            {
                for(ItemStack item : entry.items)
                {
                    this.possibleBlocks.add(SlotMachineRenderBlock.forItem(entry.getWeight(), item));
                    this.totalWeight += entry.getWeight();
                }
            }
        }
        if(this.possibleBlocks.size() == 0)
            this.possibleBlocks.add(SlotMachineRenderBlock.empty());
    }

    private void initializeLines()
    {
        //Create line entries
        while(this.lines.size() < SlotMachineEntry.ITEM_LIMIT)
            this.lines.add(new SlotMachineLine(this));
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
        {
            List<ItemStack> previousRewards = trader.getLastRewards();
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < previousRewards.size())
                    this.lines.get(i).initialize(SlotMachineRenderBlock.forItem(0, previousRewards.get(i)));
                else
                    this.lines.get(i).initialize();
            }
        }
        else
        {
            for(SlotMachineLine line : this.lines)
                line.initialize();
        }
    }

    public void tick()
    {
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null && trader.areEntriesChanged())
            this.recollectPossibleBlocks();
        if(this.menu.hasPendingReward() && this.animationTick == 0)
        {
            this.startAnimation();
            //Manually trigger the first tick as we don't quite want to rotate the
            // lines yet (partial tick will handle that just fine)
            this.animationTick++;
        }
        else if(this.animationTick > 0)
        {
            if(this.animationTick >= GetAnimationTime())
            {
                this.restTick++;
                if(this.restTick >= GetRestTime())
                {
                    this.animationTick = 0;
                    this.restTick = 0;
                    //Send message if this is the last reward known to the client to let the server know that we think we're done
                    //Shouldn't be strictly needed, but it's here as a failsafe
                    if(!this.menu.hasPendingReward())
                        this.menu.SendMessageToServer(LazyPacketData.builder().setBoolean("AnimationsCompleted", true));
                }
            }
            else
                this.animationTick();
        }
    }

    private void animationTick()
    {
        this.animationTick++;
        for(SlotMachineLine line : this.lines)
            line.animationTick();
        //Send reward flag
        if(this.animationTick >= GetAnimationTime())
        {
            this.menu.getAndRemoveNextReward();
            this.menu.SendMessageToServer(LazyPacketData.builder().setBoolean("GiveNextReward", true));
        }

    }

    private void startAnimation()
    {
        SlotMachineMenu.RewardCache pendingReward = this.menu.getNextReward();
        List<SlotMachineRenderBlock> resultBlocks = new ArrayList<>(SlotMachineEntry.ITEM_LIMIT);
        List<ItemStack> displayItems = pendingReward.getDisplayItems();
        for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
        {
            if(i < displayItems.size())
            {
                ItemStack item = displayItems.get(i);
                if(item.isEmpty())
                {
                    resultBlocks.add(SlotMachineRenderBlock.empty());
                    //LightmansCurrency.LogDebug("ResultBlock[" + i + "] set to empty as the display item was empty.");
                }
                else
                {
                    resultBlocks.add(SlotMachineRenderBlock.forItem(0, item));
                    //LightmansCurrency.LogDebug("ResultBLock[" + i + "] set to " + item.getHoverName() + ".");
                }
            }
            else
            {
                resultBlocks.add(SlotMachineRenderBlock.empty());
                //LightmansCurrency.LogDebug("ResultBlock[" + i + "] set to empty as the display item list was too short.");
            }
        }
        //Unlock the lines
        for(SlotMachineLine line : this.lines)
            line.unlock();

        //Randomize the line lock times
        RandomSource rand = this.getRandom();
        List<SlotMachineLine> randomLines = new ArrayList<>(this.lines);
        //At least 1 line should stop at the exact animation end
        List<Integer> lockDelay = Lists.newArrayList(GetAnimationTime() - 1);
        while(lockDelay.size() < randomLines.size())
            lockDelay.add(rand.nextInt(GetAnimationTime() - 11, GetAnimationTime() - 1));
        while(randomLines.size() > 0)
        {
            SlotMachineLine line = randomLines.size() > 1 ? randomLines.remove(rand.nextInt(randomLines.size())) : randomLines.remove(0);
            line.lockAtResult(resultBlocks.remove(0), lockDelay.remove(0));
        }
    }

    public void render(@Nonnull EasyGuiGraphics gui)
    {

        int startX = (this.screen.getXSize()/2) - ((SlotMachineEntry.ITEM_LIMIT * SlotMachineLine.BLOCK_SIZE)/2);
        int y = 10;
        for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
        {
            this.lines.get(i).render(gui, startX, y);
            startX += SlotMachineLine.BLOCK_SIZE;
        }

        //Render overlay on top of the rendered lines to overlap with items only just entering the viewport
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1f,1f,1f,1f);

        //Move in from of the previously rendered lines
        gui.pushPose().TranslateToForeground();
        gui.blit(GUI_TEXTURE, 0, 0, 0, 0, this.screen.getXSize(), this.screen.getYSize());
        gui.popPose();

    }

}
