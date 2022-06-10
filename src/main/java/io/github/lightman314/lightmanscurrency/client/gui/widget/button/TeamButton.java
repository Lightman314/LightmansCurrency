package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TeamButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/teambutton.png");
	
	
	public enum Size { WIDE(180, 0), NORMAL(156, 1), NARROW(90, 2);
		public final int width;
		public final int guiPos;
		Size(int width, int guiPos) {
			this.width = width;
			this.guiPos = guiPos * HEIGHT * 2;
		}
	}
	
	public static final int HEIGHT = 20;
	public static final int TEXT_COLOR = 0xFFFFFF;
	
	private final Font font;
	private final Size size;
	private final Supplier<Team> teamSource;
	public Team getTeam() { return this.teamSource.get(); }
	private final Supplier<Boolean> selectedSource;
	
	public TeamButton(int x, int y, Size size, OnPress press, Font font, @Nonnull Supplier<Team> teamSource, @Nonnull Supplier<Boolean> selectedSource)
	{
		super(x, y, size.width, HEIGHT, Component.empty(), press);
		this.font = font;
		this.size = size;
		this.teamSource = teamSource;
		this.selectedSource = selectedSource;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		if(!this.visible || this.getTeam() == null)
			return;
		
		//Render Background
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.blit(pose, this.x, this.y, 0, (selectedSource.get() ? HEIGHT : 0) + this.size.guiPos, this.size.width, HEIGHT);
		
		//Render Team Name
		this.font.draw(pose, TextRenderUtil.fitString(this.getTeam().getName(), this.width - 4), this.x + 2, this.y + 2, TEXT_COLOR);
		//Render Owner Name)
		this.font.draw(pose, TextRenderUtil.fitString(Component.translatable("gui.button.lightmanscurrency.team.owner", this.getTeam().getOwner().lastKnownName()), this.width - 4), this.x + 2, this.y + 10, TEXT_COLOR);
		
	}
	
	@Override
	public void playDownSound(SoundManager soundManager) {
		if(!this.visible || this.getTeam() == null)
			return;
		super.playDownSound(soundManager);
	}
	
}
