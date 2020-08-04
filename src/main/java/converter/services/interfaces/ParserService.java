package converter.services.interfaces;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface ParserService {

    void initParseXML() throws ParserConfigurationException, SAXException, IOException;
}
