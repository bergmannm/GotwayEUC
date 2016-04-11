package app.gotway.euc.data;

import android.support.annotation.Nullable;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import app.gotway.euc.ble.DataParser;
import app.gotway.euc.util.DebugLogger;

public class RecordStats {
    public static final String NAME = "name";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String TOTAL_TIME = "totalTime";
    public static final String MOVING_TIME = "movingTime";
    public static final String AVG_SPEED = "avgSpeed";
    public static final String MAX_SPEED = "maxSpeed";
    public static final String AVG_MOVING_SPEED = "avgMovingSpeed";
    public static final String ENERGY_USED = "energyUsed";
    public static final String ENERGY_CONSUMPTION = "energyConsumption";
    public String name;

    public Date startTime;

    public Date endTime;

    /**
     * elapsed time, ms
     */
    long totalTime;

    /**
     * elapsed time, ms
     */
    long movingTime;

    /**
     * speed, m/s
     */
    float avgSpeed;

    /**
     * speed, m/s
     */
    float maxSpeed;

    /**
     * speed, m/s
     */
    float avgMovingSpeed;

    /**
     * Wh
     */
    float energyUsed;


    /**
     * Wh/km
     */
    float energyConsumption;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public void serialize(OutputStream out) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("RecordStats");
            doc.appendChild(root);

            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

            createChildNode(doc, root, NAME, name);
            createChildNode(doc, root, START_TIME, df.format(startTime));
            createChildNode(doc, root, END_TIME, df.format(endTime));
            createChildNode(doc, root, TOTAL_TIME, totalTime);
            createChildNode(doc, root, MOVING_TIME, movingTime);
            createChildNode(doc, root, AVG_SPEED, avgSpeed);
            createChildNode(doc, root, MAX_SPEED, maxSpeed);
            createChildNode(doc, root, AVG_MOVING_SPEED, avgMovingSpeed);
            createChildNode(doc, root, ENERGY_USED, energyUsed);
            createChildNode(doc, root, ENERGY_CONSUMPTION, energyConsumption);

            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.transform(domSource, result);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static RecordStats deserialize(InputStream in) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            int eventType = parser.getEventType();
            StringBuilder sb = new StringBuilder();
            RecordStats rs = new RecordStats();
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    sb.setLength(0);
                } else if(eventType == XmlPullParser.END_TAG) {
                    String s = sb.toString();
                    switch(parser.getName()) {
                        case NAME:
                            rs.name = s;
                            break;
                        case START_TIME:
                            rs.startTime = parseDate(df, s);
                            break;
                        case END_TIME:
                            rs.endTime = parseDate(df, s);
                            break;
                        case TOTAL_TIME:
                            rs.totalTime = parseLong(s);
                            break;
                        case MOVING_TIME:
                            rs.movingTime = parseLong(s);
                            break;
                        case AVG_SPEED:
                            rs.avgSpeed = parseFloat(s);
                            break;
                        case MAX_SPEED:
                            rs.maxSpeed = parseFloat(s);
                            break;
                        case AVG_MOVING_SPEED:
                            rs.avgMovingSpeed = parseFloat(s);
                            break;
                        case ENERGY_USED:
                            rs.energyUsed = parseFloat(s);
                            break;
                        case ENERGY_CONSUMPTION:
                            rs.energyConsumption = parseFloat(s);

                            break;
                    }
                } else if(eventType == XmlPullParser.TEXT) {
                    sb.append(parser.getText());
                }
                eventType = parser.next();
            }
            return rs;
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private static long parseLong(String s) {
        try {
            if (s.length()>0) {
                return Long.parseLong(s);
            }
        } catch (NumberFormatException e) {
            DebugLogger.w(RecordStats.class.getSimpleName(), e.toString(), e);
        }
        return -1;
    }

    private static float parseFloat(String s) {
        try {
            if (s.length()>0) {
                return Float.parseFloat(s);
            }
        } catch (NumberFormatException e) {
            DebugLogger.w(RecordStats.class.getSimpleName(), e.toString(), e);
        }
        return -1;
    }

    @Nullable
    private static Date parseDate(SimpleDateFormat df, String s) {
        try {
            if (s.length()>0) {
                return df.parse(s);
            }
        } catch (ParseException e) {
            DebugLogger.w(RecordStats.class.getSimpleName(), e.toString(), e);
        }
        return null;
    }


    void createChildNode(Document doc, Element parent, String name, float value) {
        createChildNode(doc, parent, name, DataParser.floatToStr(value));
    }

    void createChildNode(Document doc, Element parent, String name, long value) {
        createChildNode(doc, parent, name, Long.toString(value));
    }

    void createChildNode(Document doc, Element parent, String name, String value) {
        Text textNode = doc.createTextNode(value);
        Element element = doc.createElement(name);
        element.appendChild(textNode);
        parent.appendChild(element);
    }

}
