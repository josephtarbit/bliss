package sm0keysa1m0n.bliss.view;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import io.github.humbleui.skija.BlendMode;
import io.github.humbleui.skija.ColorFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.types.Rect;
import sm0keysa1m0n.bliss.Size;
import sm0keysa1m0n.bliss.layout.Layout;
import sm0keysa1m0n.bliss.layout.MeasureMode;

public class ImageView extends View {

  @Nullable
  private ImageAccess image;

  private Size fittedImageSize;

  public ImageView(Properties properties) {
    super(properties);
  }

  @Override
  protected void setLayout(Layout layout) {
    super.setLayout(layout);
    layout.setMeasureFunction(this::measure);
  }

  public ImageView setImage(@Nullable ImageAccess image) {
    if (this.image != null) {
      this.image.close();
    }

    this.image = image;

    if (this.hasLayout()) {
      this.getLayout().markDirty();
    }

    return this;
  }

  private Optional<Size> getFittedImageSize() {
    return this.getFittedImageSize(this.getContentWidth(), this.getContentHeight());
  }

  private Optional<Size> getFittedImageSize(float containerWidth, float containerHeight) {
    if (this.image == null) {
      return Optional.empty();
    }

    var size = this.image.getIntrinsicSize(containerWidth, containerHeight);
    return Optional.of(this.getStyle().objectFit.get().getSize(
        size.getX(), size.getY(), containerWidth, containerHeight));
  }

  @Override
  public void layout() {
    super.layout();
    this.fittedImageSize = this.getFittedImageSize().orElse(null);
    if (this.fittedImageSize != null) {
      this.image.prepare(this.fittedImageSize.width() * this.graphicsContext.scale(),
          this.fittedImageSize.height() * this.graphicsContext.scale());
    }
  }

  private Size measure(MeasureMode widthMode, float width, MeasureMode heightMode, float height) {
    return this
        .getFittedImageSize(widthMode == MeasureMode.UNDEFINED ? Integer.MAX_VALUE : width,
            heightMode == MeasureMode.UNDEFINED ? Integer.MAX_VALUE : height)
        .orElse(new Size(width, height));
  }

  @Override
  public void renderContent(int mouseX, int mouseY) {
    super.renderContent(mouseX, mouseY);

    var canvas = this.graphicsContext.canvas();

    if (this.image == null) {
      try (var paint = new Paint()) {
        paint.setAlphaf(this.getAlpha()).setColor(0xFFFFFF).setMode(PaintMode.FILL);
        var scale = this.graphicsContext.scale();
        canvas.drawRect(Rect.makeXYWH(
            this.getScaledContentX() * scale,
            this.getScaledContentY() * scale,
            this.getScaledContentWidth() * scale,
            this.getScaledContentHeight() * scale), paint);
      }
      return;
    }

    canvas.translate(
        this.getScaledContentX() * this.graphicsContext.scale(),
        this.getScaledContentY() * this.graphicsContext.scale());
    canvas.scale(this.getXScale(), this.getYScale());

    try (var paint = new Paint()) {
      paint.setColorFilter(ColorFilter.makeBlend(
          this.getStyle().color.get().multiplied(this.getAlpha()),
          BlendMode.MODULATE));
      this.image.draw(canvas, paint, this.getStyle().imageRendering.get());
    }
    canvas.resetMatrix();
  }

  @Override
  public void close() {
    super.close();
    if (this.image != null) {
      this.image.close();
      this.image = null;
    }
  }
}
