/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataset.MeshLib;
import utility.Utilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author c1038943
 */
public class MeSHParser extends DefaultHandler {

    Vector<String> inTag = new Vector<String>();
    String DescriptorRecordID = "";
    String ConceptID = "";
    Connection conn = null;
    Statement stmt = null;
    String SemanticTypeUIID = "";
    private String TermUIID = "";
    String q = "";

    public MeSHParser(Connection _conn) {
        try {
            conn = _conn;
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void BeginParse(String path) {
        System.out.print("Loading MeSH database...\n");
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse(path, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.print("\nFinished importing MeSH DB");
    }

    //Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        inTag.add(qName);
        //System.out.println("In " + qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            String value = new String(ch, start, length);
            value = value.replace("'", "''");
            if (inTag.indexOf("DescriptorRecordSet") >= 0
                    && inTag.indexOf("DescriptorRecord") >= 0
                    && inTag.indexOf("DescriptorUI") >= 0
                    && inTag.indexOf("PharmacologicalAction") < 0) {
                DescriptorRecordID = value;
            } else if (inTag.indexOf("DescriptorRecordSet") >= 0
                    && inTag.indexOf("DescriptorRecord") >= 0
                    && inTag.indexOf("DescriptorName") >= 0
                    && inTag.indexOf("String") >= 0
                    && inTag.indexOf("PharmacologicalAction") < 0) {
                System.out.print(".");
                if (!DescriptorRecordID.equals("") && !value.equals("")) {
                    q = "insert ignore into record (id,record_string) values ('" + DescriptorRecordID + "','" + value + "')";
                    stmt.execute(q);
                }
            } else if (inTag.indexOf("TreeNumberList") >= 0
                    && inTag.indexOf("TreeNumber") >= 0) {
                if (!DescriptorRecordID.equals("") && !value.equals("")) {
                    q = "insert ignore into record_node (node_string,record_id) values ('" + value + "','" + DescriptorRecordID + "')";
                    stmt.execute(q);
                }
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("ConceptUI") >= 0) {
                ConceptID = value;
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("ConceptName") >= 0
                    && inTag.indexOf("String") >= 0) {
                if (!DescriptorRecordID.equals("") && !value.equals("") && !ConceptID.equals("")) {
                    q = "insert ignore into concept (concept_id,record_id,concept_string) values ('" + ConceptID + "','" + DescriptorRecordID + "','" + value + "')";
                    stmt.execute(q);
                }
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("SemanticTypeList") >= 0
                    && inTag.indexOf("SemanticTypeUI") >= 0) {
                SemanticTypeUIID = value;
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("SemanticTypeList") >= 0
                    && inTag.indexOf("SemanticTypeName") >= 0) {
                if (!SemanticTypeUIID.equals("") && !value.equals("") && !ConceptID.equals("")) {
                    q = "insert ignore into semantic (sem_id,sem_string,concept_id) values ('" + SemanticTypeUIID + "','" + value + "','" + ConceptID + "')";
                    stmt.execute(q);
                }
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("TermList") >= 0
                    && inTag.indexOf("TermUI") >= 0) {
                TermUIID = value;
            } else if (inTag.indexOf("ConceptList") >= 0
                    && inTag.indexOf("TermList") >= 0
                    && inTag.indexOf("String") >= 0) {
                if (!TermUIID.equals("") && !value.equals("") && !ConceptID.equals("")) {
                    q = "insert ignore into terms (term_id,term_string,concept_id) values ('" + TermUIID + "','" + value + "','" + ConceptID + "')";
                    stmt.execute(q);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Utilities.WriteLogTrace(ex);
            Utilities.WriteLog(q);
        }
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {
        inTag.remove(qName);
        //System.out.println("out " + qName);
    }
}
