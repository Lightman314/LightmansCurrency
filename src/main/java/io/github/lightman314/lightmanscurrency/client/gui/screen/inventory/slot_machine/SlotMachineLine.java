package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nonnull;

public final class SlotMachineLine {

    public static int BLOCK_SIZE = 18;

    private final SlotMachineRenderer parent;
    public SlotMachineLine(SlotMachineRenderer parent) { this.parent = parent; }

    //Starts locked
    int lockDelay = 0;

    SlotMachineRenderBlock resultBlock = SlotMachineRenderBlock.empty();

    SlotMachineRenderBlock previousBlock2 = SlotMachineRenderBlock.empty();
    SlotMachineRenderBlock previousBlock1 = SlotMachineRenderBlock.empty();
    SlotMachineRenderBlock centerBlock = SlotMachineRenderBlock.empty();
    SlotMachineRenderBlock nextBlock = SlotMachineRenderBlock.empty();

    public void render(@Nonnull EasyGuiGraphics gui, int x, int y)
    {
        //Limit partial tick to be <= 1 so that the items don't clip beyond the overlay
        float partialTick = MathUtil.clamp(gui.partialTicks, 0f, 1f);
        if(this.lockDelay != 0)
            y += (int)((float)BLOCK_SIZE * partialTick);
        this.previousBlock2.render(gui, ++x, ++y);
        this.previousBlock1.render(gui, x, y + BLOCK_SIZE);
        this.centerBlock.render(gui, x, y + (2 * BLOCK_SIZE));
        this.nextBlock.render(gui, x, y + (3 * BLOCK_SIZE));
    }

    public void initialize() { this.initialize(SlotMachineRenderBlock.empty()); }
    public void initialize(@Nonnull SlotMachineRenderBlock previousReward)
    {
        this.previousBlock2 = this.parent.getRandomBlock();
        this.previousBlock1 = this.parent.getRandomBlock();
        this.centerBlock = previousReward;
        this.nextBlock = this.parent.getRandomBlock();
    }

    public void animationTick()
    {
        if(this.lockDelay > 0)
        {
            this.lockDelay--;
            if(this.lockDelay == 2)
            {
                //Set the previousBlock2 to the result item 2 ticks before locking
                this.rotateBlocks(this.resultBlock);
                return;
            }
            if(this.lockDelay == 0)
                this.playDing();
        }
        else if(this.lockDelay == 0)
            return;
        this.rotateBlocks();
    }

    private void playDing()
    {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if(soundManager != null)
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f));
    }

    private void rotateBlocks() { this.rotateBlocks(this.parent.getRandomBlock()); }
    private void rotateBlocks(@Nonnull SlotMachineRenderBlock newBlock)
    {
        this.nextBlock = this.centerBlock;
        this.centerBlock = this.previousBlock1;
        this.previousBlock1 = this.previousBlock2;
        this.previousBlock2 = newBlock;
    }

    //Call 20 ticks before
    public void lockAtResult(@Nonnull SlotMachineRenderBlock block, int lockDelay)
    {
        this.lockDelay = lockDelay;
        this.resultBlock = block;
    }

    //Called at the start of the animation
    public void unlock() { this.lockDelay = -1; this.resultBlock = SlotMachineRenderBlock.empty(); }

}
