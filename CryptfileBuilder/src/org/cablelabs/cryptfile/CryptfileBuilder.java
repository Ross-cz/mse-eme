
package org.cablelabs.cryptfile;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to generate "cryptfiles" for MP4Box media encryption
 */
public class CryptfileBuilder {
    
    private ProtectionScheme scheme;
    private List<DRMInfoPSSH> pssh;
    private List<CryptTrack> tracks;
    
    private static final String ELEMENT = "GPACDRM";
    private static final String ATTR_TYPE = "type";

    /**
     * Possible encryption schemes under Common Encryption
     */
    public enum ProtectionScheme {
        AES_CTR("AES-CTR"),
        AES_CBC("AES-CBC");
        
        private String str;
        
        ProtectionScheme(String str) {
            this.str = str;
        }
        
        public String toString() {
            return str;
        }
    }
    
    /**
     * Create a new cryptfile builder with PSSH and track info
     * 
     * @param scheme the desired encryption scheme
     * @param tracks the track list
     * @param pssh the PSSH list
     */
    public CryptfileBuilder(ProtectionScheme scheme, List<CryptTrack> tracks, List<DRMInfoPSSH> pssh) {
        this(scheme);
        this.tracks = tracks;
        this.pssh = pssh;
    }
    
    /**
     * Create a new cryptfile builder
     * 
     * @param scheme the desired encryption scheme
     */
    public CryptfileBuilder(ProtectionScheme scheme) {
        this.scheme = scheme;
        pssh = new ArrayList<DRMInfoPSSH>();
        tracks = new ArrayList<CryptTrack>();
    }
    
    /**
     * Add a single track 
     * 
     * @param track the track to add
     */
    public void addTrack(CryptTrack track) {
        tracks.add(track);
    }
    
    /**
     * Add multiple tracks 
     * 
     * @param tracks the tracks to add
     */
    public void addTracks(List<CryptTrack> tracks) {
        tracks.addAll(tracks);
    }
    
    /**
     * Add a single PSSH 
     * 
     * @param pssh the PSSH to add
     */
    public void addPSSH(DRMInfoPSSH pssh) {
        this.pssh.add(pssh);
    }
    
    /**
     * Add multiple PSSH
     * 
     * @param pssh the list of PSSH to add
     */
    public void addPSSH(List<DRMInfoPSSH> pssh) {
        this.pssh.addAll(pssh);
    }
    
    /**
     * 
     * @param os
     */
    public void writeCryptfile(OutputStream os) {
        
        // Create a new document
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException ex) {
            System.out.println("Error creating XML DocumentBuilder: " + ex.getMessage());
            System.exit(1);
        }
        Document d = builder.newDocument();
        
        // Create our root node
        Element e = d.createElement(ELEMENT);
        e.setAttribute(ATTR_TYPE, "CENC " + scheme.toString());
        
        // Add all the child elements (DRMInfo and CryptTracks)
        List<MP4BoxXML> elements = new ArrayList<MP4BoxXML>(tracks.size() + pssh.size());
        elements.addAll(pssh);
        elements.addAll(tracks);
        for (MP4BoxXML xml : elements) {
            e.appendChild(xml.generateXML(d));
        }
        
        // Add the root node to our document
        d.appendChild(e);
        
        // Write the document to the desired output
        Transformer tf = null;
        try {
            tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        catch (Exception ex) {
            System.out.println("Error creating XML Transformer: " + ex.getMessage());
            System.exit(1);;
        }
        
        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(os);
        try {
            tf.transform(source, result);
        }
        catch (TransformerException ex) {
            System.out.println("Error performing XML transform: " + ex.getMessage());
            System.exit(1);;
        }
    }
    
}
