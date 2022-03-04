package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import javax.annotation.Nonnull;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	
	private final FontRenderer font;
	private final Size size;
	private final Supplier<Team> teamSource;
	public Team getTeam() { return this.teamSource.get(); }
	private final Supplier<Boolean> selectedSource;
	
	public TeamButton(int x, int y, Size size, IPressable press, FontRenderer font, @Nonnull Supplier<Team> teamSource, @Nonnull Supplier<Boolean> selectedSource)
	{
		super(x, y, size.width, HEIGHT, new StringTextComponent(""), press);
		this.font = font;
		this.size = size;
		this.teamSource = teamSource;
		this.selectedSource = selectedSource;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		if(!this.visible || this.getTeam() == null)
			return;
		
		//Render Background
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		
		this.blit(pose, this.x, this.y, 0, (selectedSource.get() ? HEIGHT : 0) + this.size.guiPos, this.size.width, HEIGHT);
		
		//Render Team Name
		this.font.drawString(pose, this.fitString(this.getTeam().getName()), this.x + 2, this.y + 2, TEXT_COLOR);
		//Render Owner Name)
		this.font.drawString(pose, this.fitString(new TranslationTextComponent("gui.button.lightmanscurrency.team.owner", this.getTeam().getOwner().lastKnownName()).getString()), this.x + 2, this.y + 10, TEXT_COLOR);
		
	}
	
	private String fitString(String string)
	{
		//If it already fits, don't bother editing it
		if(this.font.getStringWidth(string) <= this.width - 4)
			return string;
		//Shorten 1 char at a time, but assume a ... will be at the end
		while(this.font.getStringWidth(string + "...") > this.width - 4)
			string = string.substring(0, string.length() - 1);
		return string + "...";
	}
	
}
