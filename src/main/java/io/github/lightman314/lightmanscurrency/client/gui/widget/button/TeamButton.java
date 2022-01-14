package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class TeamButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/teambutton.png");
	
	public static final int WIDTH = 180;
	public static final int HEIGHT = 20;
	public static final int TEXT_COLOR = 0xFFFFFF;
	
	private final Font font;
	private final Supplier<Team> teamSource;
	public Team getTeam() { return this.teamSource.get(); }
	private final Supplier<Boolean> selectedSource;
	
	public TeamButton(int x, int y, OnPress press, Font font, @Nonnull Supplier<Team> teamSource, @Nonnull Supplier<Boolean> selectedSource)
	{
		super(x, y, WIDTH, HEIGHT, new TextComponent(""), press);
		this.font = font;
		this.teamSource = teamSource;
		this.selectedSource = selectedSource;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		this.visible = this.getTeam() != null;
		if(!this.visible)
			return;
		
		//Render Background
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		this.blit(pose, this.x, this.y, 0, selectedSource.get() ? HEIGHT : 0, WIDTH, HEIGHT);
		
		//Render Team Name
		this.font.draw(pose, this.getTeam().getName(), this.x + 2, this.y + 2, TEXT_COLOR);
		//Render Owner Name)
		this.font.draw(pose, new TranslatableComponent("gui.button.lightmanscurrency.team.owner", this.getTeam().getOwner().lastKnownName()), this.x + 2, this.y + 10, TEXT_COLOR);
		
	}
	
}
