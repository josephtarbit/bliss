package sm0keysa1m0n.bliss.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class LoggingErrorHandler implements ErrorHandler {

  public static final LoggingErrorHandler INSTANCE = new LoggingErrorHandler();

  private static final Logger logger = LoggerFactory.getLogger(LoggingErrorHandler.class);

  private LoggingErrorHandler() {}

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    logger.warn("[SAX Parser Warning] ", exception);
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    logger.error("[SAX Parser Error] ", exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    logger.error("[SAX Parser Fatal Error] ", exception);
  }
}
