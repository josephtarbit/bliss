package sm0keysa1m0n.bliss.minecraft.view;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import sm0keysa1m0n.bliss.minecraft.RenderUtil;
import sm0keysa1m0n.bliss.view.View;

public class PanoramaView extends View {

  private final PanoramaRenderer panorama;

  public PanoramaView(Properties properties, CubeMap cubeMap) {
    super(properties);
    Objects.requireNonNull(cubeMap, "cubeMap cannot be null");
    this.panorama = new PanoramaRenderer(cubeMap);
  }

  @Override
  public void renderContent(int mouseX, int mouseY) {
    super.renderContent(mouseX, mouseY);
    this.graphicsContext.exitManaged();
    {
      var minecraft = Minecraft.getInstance();
      if (minecraft.level == null) {
        this.panorama.render(
            ((MinecraftViewScreen) this.getScreen()).getPartialTick(), this.getAlpha());
      } else {
        RenderUtil.fillGradient(0, 0, this.getScaledContentWidth(),
            this.getScaledContentHeight(), 0xA0101010, 0xB0101010);
      }
    }
    this.graphicsContext.enterManaged();
  }
}
