package org.trec.liveqa;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Copyright 2015 Yahoo Inc.<br>
 * Licensed under the terms of the MIT license. Please see LICENSE file at the root of this project for terms.
 * <p/>
 *
 * @author yuvalp@yahoo-inc.com
 * 
 */
public class XmlUtils {

    /**
     * Creates a new element, with the given text inside that element.
     * 
     * @param document the document into which the element is added.
     * @param parentElement The parent element of the element-to-be-added.
     * @param newElementName name of new element (tag-name).
     * @param textString the text to be written inside the newly created element.
     * @return The new element.
     */
    public static Element addElementWithText(Document document, Element parentElement, String newElementName,
                    String textString) {
        Element newElement = document.createElement(newElementName);
        Text text = document.createTextNode(textString);
        newElement.appendChild(text);
        parentElement.appendChild(newElement);
        return newElement;
    }


    public static String writeDocumentToString(Document document) {
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans;
            trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            try {
                StreamResult streamResult = new StreamResult(writer);
                DOMSource source = new DOMSource(document);
                trans.transform(source, streamResult);
            } catch (TransformerException e) {
                e.printStackTrace();
                return null;
            } finally {
                writer.close();
            }
            return writer.toString();
            
        } catch (TransformerConfigurationException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}