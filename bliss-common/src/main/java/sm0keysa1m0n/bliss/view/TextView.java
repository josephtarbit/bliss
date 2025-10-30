package sm0keysa1m0n.bliss.view;

import org.jetbrains.annotations.Nullable;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PictureRecorder;
import io.github.humbleui.skija.paragraph.DecorationStyle;
import io.github.humbleui.skija.paragraph.FontCollection;
import io.github.humbleui.skija.paragraph.Paragraph;
import io.github.humbleui.skija.paragraph.ParagraphBuilder;
import io.github.humbleui.skija.paragraph.ParagraphStyle;
import io.github.humbleui.skija.paragraph.TextStyle;
import io.github.humbleui.types.Rect;
import sm0keysa1m0n.bliss.Size;
import sm0keysa1m0n.bliss.StyledText;
import sm0keysa1m0n.bliss.TextDecoration;
import sm0keysa1m0n.bliss.TextVisitor;
import sm0keysa1m0n.bliss.layout.Layout;
import sm0keysa1m0n.bliss.layout.MeasureMode;
import sm0keysa1m0n.bliss.util.MathUtil;

public class TextView extends View {

  private TextVisitor text = __ -> {};
  private boolean wrap = true;

  @Nullable
  private Paragraph paragraph = null;
  private int textCount;

  private FontMgr fontManager = FontMgr.getDefault();

  public TextView(Properties properties) {
    super(properties);
    this.getStyle().color.addListener(color -> {
      if (this.paragraph != null && this.isAdded()) {
        this.paragraph.updateForegroundPaint(0, this.textCount,
            new Paint().setColor(color.hex()));
        this.paragraph.layout(this.getContentWidth() * this.graphicsContext.scale());
      }
    });
    this.getStyle().fontFamily.addListener(__ -> this.buildParagraph());
    this.getStyle().fontSize.addListener(fontSize -> {
      if (this.paragraph != null && this.isAdded()) {
        this.paragraph.updateFontSize(0, this.textCount, fontSize);
        this.paragraph.layout(this.getContentWidth() * this.graphicsContext.scale());
      }
    });
    this.getStyle().textAlign.addListener(textAlign -> {
      if (this.paragraph != null && this.isAdded()) {
        this.paragraph.updateAlignment(textAlign);
        this.paragraph.layout(this.getContentWidth() * this.graphicsContext.scale());
      }
    });
    this.getStyle().textShadow.addListener(__ -> this.buildParagraph());
  }

  @Override
  public void styleRefreshed(FontMgr fontManager) {
    if (this.fontManager != FontMgr.getDefault()) {
      this.fontManager.close();
    }
    this.fontManager = fontManager;
    this.buildParagraph();
  }

  @Override
  protected void setLayout(Layout layout) {
    super.setLayout(layout);
    layout.setMeasureFunction(this::measure);
  }

  public TextView setWrap(boolean wrap) {
    this.wrap = wrap;
    if (this.isAdded()) {
      this.getLayout().markDirty();
      this.parent.layout();
    }
    return this;
  }

  public TextView setText(@Nullable String text) {
    return this.setText(StyledText.of(text));
  }

  public TextView setText(StyledText text) {
    return this.setText(TextVisitor.of(text));
  }

  public TextView setText(StyledText text, TextDecoration decoration) {
    return this.setText(TextVisitor.of(text, decoration));
  }

  public TextView setText(TextVisitor text) {
    this.text = text;
    this.buildParagraph();
    if (this.isAdded()) {
      this.getLayout().markDirty();
      this.parent.layout();
    }
    return this;
  }

  @SuppressWarnings("resource")
  private void buildParagraph() {
    if (this.paragraph != null) {
      this.paragraph.close();
      this.paragraph = null;
    }
    this.textCount = 0;

    try (var paragraphStyle = new ParagraphStyle()
        .setAlignment(this.getStyle().textAlign.get())
        .setEllipsis("...")
        .setMaxLinesCount(this.wrap ? Integer.MAX_VALUE : 1);
        var fontCollection = new FontCollection()
            .setDefaultFontManager(this.fontManager)
            .setDynamicFontManager(FontMgr.getDefault());
        var builder = new ParagraphBuilder(paragraphStyle, fontCollection)) {
      this.text.visit((styledText, decoration) -> {
        var color = styledText.color() == null
            ? this.getStyle().color.get().hex()
            : styledText.color().hex();
        try (var textStyle = new TextStyle()
            .setFontSize(this.getStyle().fontSize.get() * this.graphicsContext.scale())
            .setFontFamilies(this.getStyle().fontFamily.get())
            .addShadows(this.getStyle().textShadow.get())
            .setColor(color)
            .setFontStyle(styledText.fontStyle())
            .setDecorationStyle(DecorationStyle.NONE
                .withUnderline(decoration.underline())
                .withLineThrough(decoration.lineThrough())
                .withColor(color))) {
          builder.pushStyle(textStyle);
          builder.addText(styledText.text());
          this.textCount += styledText.text().length();
          builder.popStyle();
        }
      });
      this.paragraph = builder.build();
    }
  }

  @Override
  public void layout() {
    if (this.paragraph != null) {
      this.paragraph.updateFontSize(0, this.textCount,
          this.getStyle().fontSize.get() * this.graphicsContext.scale());
      this.paragraph.layout(this.getContentWidth() * this.graphicsContext.scale());
    }
    super.layout();
  }

  private Size measure(MeasureMode widthMode, float width, MeasureMode heightMode, float height) {
    this.paragraph.updateFontSize(0, this.textCount,
        this.getStyle().fontSize.get() * this.graphicsContext.scale());
    switch (widthMode) {
      case UNDEFINED:
      case AT_MOST:
        this.paragraph.layout(widthMode == MeasureMode.UNDEFINED
            ? Float.MAX_VALUE
            : MathUtil.ceil(width) * this.graphicsContext.scale());

        width = this.paragraph.getMaxIntrinsicWidth() / this.graphicsContext.scale();
        break;
      default:
        this.paragraph.layout(MathUtil.ceil(width) * this.graphicsContext.scale());
        break;
    }
    return new Size(width, this.paragraph.getHeight() / this.graphicsContext.scale());
  }

  @Override
  public float computeFullHeight() {
    return this.paragraph == null
        ? super.computeFullHeight()
        : this.paragraph.getHeight() / this.graphicsContext.scale();
  }

  @SuppressWarnings("resource")
  @Override
  public void renderContent(int mouseX, int mouseY) {
    super.renderContent(mouseX, mouseY);
    if (this.paragraph != null) {
      var canvas = this.graphicsContext.canvas();

      canvas.translate(
          this.getScaledContentX() * this.graphicsContext.scale(),
          this.getScaledContentY() * this.graphicsContext.scale());

      canvas.scale(this.getXScale(), this.getYScale());

      try (var recorder = new PictureRecorder()) {
        var recordingCanvas = recorder.beginRecording(
            Rect.makeWH(this.paragraph.getMaxWidth(), this.paragraph.getHeight()));
        this.paragraph.paint(recordingCanvas, 0, 0);
        var picture = recorder.finishRecordingAsPicture();
        try (var paint = new Paint().setAlphaf(this.getAlpha())) {
          canvas.drawPicture(picture, null, paint);
        }
      }

      canvas.resetMatrix();
    }
  }

  @Override
  public void close() {
    super.close();
    if (this.paragraph != null) {
      this.paragraph.close();
      this.paragraph = null;
    }
    if (this.fontManager != FontMgr.getDefault()) {
      this.fontManager.close();
    }
  }
}
