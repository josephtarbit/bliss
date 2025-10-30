package sm0keysa1m0n.bliss.minecraft.view;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import sm0keysa1m0n.bliss.minecraft.RenderUtil;
import sm0keysa1m0n.bliss.view.View;

public class AvatarView extends View {

  private ResourceLocation textureLocation;

  public AvatarView(Properties properties, GameProfile gameProfile) {
    super(properties);
    this.textureLocation = DefaultPlayerSkin.getDefaultSkin(gameProfile.getId());
    Minecraft.getInstance().getSkinManager().registerSkins(gameProfile,
        (type, textureLocation, texture) -> {
          if (type == MinecraftProfileTexture.Type.SKIN) {
            this.textureLocation = textureLocation;
          }
        }, true);
  }

  @Override
  protected void renderContent(int mouseX, int mouseY) {
    super.renderContent(mouseX, mouseY);
    this.graphicsContext.exitManaged();
    {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.getAlpha());
      RenderUtil.blitAvatar(this.textureLocation,
          this.getScaledContentX(), this.getScaledContentY(),
          this.getScaledContentWidth(), this.getScaledContentHeight());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    this.graphicsContext.enterManaged();
  }
}
