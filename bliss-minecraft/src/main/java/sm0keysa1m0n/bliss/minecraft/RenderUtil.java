package sm0keysa1m0n.bliss.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderUtil {

  public static final int FULL_LIGHT = 0xF000F0;


  public static void fillGradient(float x, float y, float x2, float y2,
      int startColor, int endColor) {
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.setShader(GameRenderer::getPositionColorShader);

    var startAlpha = (startColor >> 24 & 255) / 255.0F;
    var startRed = (startColor >> 16 & 255) / 255.0F;
    var startGreen = (startColor >> 8 & 255) / 255.0F;
    var startBlue = (startColor & 255) / 255.0F;

    var endAlpha = (endColor >> 24 & 255) / 255.0F;
    var endRed = (endColor >> 16 & 255) / 255.0F;
    var endGreen = (endColor >> 8 & 255) / 255.0F;
    var endBlue = (endColor & 255) / 255.0F;

    var tessellator = Tesselator.getInstance();
    var builder = tessellator.getBuilder();
    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    builder
        .vertex(x, y2, 0.0F)
        .color(startRed, startGreen, startBlue, startAlpha)
        .endVertex();
    builder
        .vertex(x2, y2, 0.0F)
        .color(endRed, endGreen, endBlue, endAlpha)
        .endVertex();
    builder
        .vertex(x2, y, 0.0F)
        .color(endRed, endGreen, endBlue, endAlpha)
        .endVertex();
    builder
        .vertex(x, y, 0.0F)
        .color(startRed, startGreen, startBlue, startAlpha)
        .endVertex();
    tessellator.end();

    RenderSystem.disableBlend();
    RenderSystem.enableTexture();
  }

  public static void blitAvatar(
      ResourceLocation skin, float x, float y, float width, float height) {
    RenderSystem.setShaderTexture(0, skin);
    spriteBlit(x, y, width, height, 8.0F, 8.0F, 8, 8, 64, 64);
  }

  public static void spriteBlit(float x, float y,
      float width, float height, float spriteX, float spriteY, float spriteWidth,
      float spriteHeight, float textureWidth, float textureHeight) {
    spriteBlit(x, y, x + width, y + height, 0, spriteWidth, spriteHeight, spriteX,
        spriteY, textureWidth, textureHeight);
  }

  public static void spriteBlit(float x, float y, float x2,
      float y2, float z, float spriteWidth, float spriteHeight,
      float spriteX, float spriteY, float textureWidth, float textureHeight) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    blit(x, y, x2, y2,
        spriteX / textureWidth,
        (spriteY + spriteHeight) / textureHeight,
        (spriteX + spriteWidth) / textureWidth,
        spriteY / textureHeight);
  }

  public static void blit(float x, float y, float x2, float y2,
      float u, float v, float u2, float v2) {
    final var tesselator = Tesselator.getInstance();
    final var builder = tesselator.getBuilder();
    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    builder.vertex(x, y2, 0.0F).uv(u, v).endVertex();
    builder.vertex(x2, y2, 0.0F).uv(u2, v).endVertex();
    builder.vertex(x2, y, 0.0F).uv(u2, v2).endVertex();
    builder.vertex(x, y, 0.0F).uv(u, v2).endVertex();
    tesselator.end();
  }
}
