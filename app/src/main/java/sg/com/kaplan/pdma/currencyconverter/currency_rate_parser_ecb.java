package sg.com.kaplan.pdma.currencyconverter;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class currency_rate_parser_ecb {
    // This variable is used for debug log (LogCat)
    private static final String TAG = "CC:parser_ecb";

    private List<currency_rate> data = new ArrayList<currency_rate>();
    private XMLReader xr;

    private Boolean createParser() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            xr = sp.getXMLReader();
            MyHandler handler = new MyHandler();
            xr.setContentHandler(handler);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "createParser:" + e.toString());
            data.clear();
        }

        return false;
    }

    public List<currency_rate> getRates() {
        return data;
    }

    public boolean StartParser(String szURL) {
        if (createParser() == true) {
            try {
                URL url = new URL(szURL);
                InputStream stream = url.openStream();
                xr.parse(new InputSource(stream));
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Cannot start parser for Input steam");
                data.clear();
            }
        }

        return false;
    }

    public boolean StartParser(Context context, int raw_src_id) {
        Resources res = context.getResources();

        if (createParser() == true) {
            try {
                xr.parse(new InputSource(res.openRawResource(raw_src_id)));
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Cannot start parser for resource " + Integer.toString(raw_src_id));
                data.clear();
            }
        }

        return false;
    }

    public class currency_rate {
        public String m_name = "";
        public double m_rate = 0;
    }

    private class MyHandler extends DefaultHandler {
        @Override
        public void startDocument() throws SAXException {
            Log.d(TAG, "***** start document *****");
        }

        @Override
        public void endDocument() throws SAXException {
            Log.d(TAG, "***** end document *****");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currency_rate rate_data;
            String name = "EUR";
            double rate = 1.0;

            Log.d(TAG, "start element: localname=" + localName);
            for (int i = 0; i < attributes.getLength(); i++) {
                Log.d(TAG, "start element: attr=" + attributes.getLocalName(i) + " value=" + attributes.getValue(i));
            }

            if (localName == "Cube") {
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getLocalName(i) == "currency") {
                        name = attributes.getValue(i);
                    } else if (attributes.getLocalName(i) == "rate") {
                        try {
                            rate = Double.parseDouble(attributes.getValue(i));
                        } catch (Exception e) {
                            Log.e(TAG, "startElement:" + e.toString());
                            rate = 1.0;
                        }

                        // create a new element
                        rate_data = new currency_rate();
                        rate_data.m_name = name;
                        rate_data.m_rate = rate;

                        // add new element in the list
                        data.add(rate_data);
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            Log.d(TAG, "end element: localname=" + localName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            //String content = new String(ch, start, length);
            //Log.d(TAG, "content=" + content);
        }
    }
}
