package sm0keysa1m0n.bliss;

public enum ObjectFit {

  FILL, COVER {
    @Override
    public Size getSize(float contentWidth, float contentHeight, float containerWidth,
        float containerHeight) {
      float widthScale = containerWidth / contentWidth;
      float heightScale = containerHeight / contentHeight;
      float finalScale = contentHeight * widthScale < containerHeight ? heightScale : widthScale;
      return new Size(contentWidth * finalScale, contentHeight * finalScale);
    }
  },
  CONTAIN {
    @Override
    public Size getSize(float contentWidth, float contentHeight, float containerWidth,
        float containerHeight) {
      float widthScale = containerWidth / contentWidth;
      float heightScale = containerHeight / contentHeight;
      float finalScale = contentHeight * widthScale > containerHeight ? heightScale : widthScale;
      if (finalScale * contentWidth > containerWidth) {
        contentWidth = finalScale * contentWidth;
        contentHeight = finalScale * contentHeight;
        finalScale = containerWidth / (contentWidth);
      }
      return new Size(contentWidth * finalScale, contentHeight * finalScale);
    }
  },
  NONE {
    @Override
    public Size getSize(float contentWidth, float contentHeight, float containerWidth,
        float containerHeight) {
      return new Size(contentWidth, contentHeight);
    }
  };

  public Size getSize(float contentWidth, float contentHeight, float containerWidth,
      float containerHeight) {
    return new Size(containerWidth, containerHeight);
  }
}
