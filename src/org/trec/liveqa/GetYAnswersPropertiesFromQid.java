package org.trec.liveqa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Copyright 2015 Yahoo Inc.<br>
 * Licensed under the terms of the MIT license. Please see LICENSE file at the root of this project for terms.
 * <p/>
 * 
 * This class scrapes Yahoo Answers pages by a list of QIDs, obtaining certain properties.
 * 
 * @author yuvalp@yahoo-inc.com
 * 
 */
public class GetYAnswersPropertiesFromQid {

    public interface ElementPredicate {

        /**
         * Given an HTML Element, see if it conforms to a Yahoo Answers tag pattern specific to the desired property.
         * @param e
         * @return
         */
        public boolean check(Element e);
        
        public String getText(Element e);

    }

    public static class CheckTitle implements ElementPredicate {

        @Override
        public boolean check(Element e) {
            return e.nodeName().equals("meta") && e.attr("name").equals("title");
        }

        @Override
        public String getText(Element e) {
            return e.attr("content");
        }

    }

    public static class CheckBody implements ElementPredicate {

        @Override
        public boolean check(Element e) {
            return e.nodeName().equals("meta") && e.attr("name").equals("description");
        }

        @Override
        public String getText(Element e) {
            return e.attr("content");
        }

    }

    public static class CheckBestAnswer implements ElementPredicate {

        @Override
        public boolean check(Element e) {
            // implement your own...
            return false;
        }

        @Override
        public String getText(Element e) {
            // implement your own...
            return null;
        }

    }

    public static class CheckTopLevelCategory implements ElementPredicate {

        @Override
        public boolean check(Element e) {
            return e.nodeName().equals("a") && e.className().contains("Clr-b");
        }

        @Override
        public String getText(Element e) {
            return e.text();
        }

    }

    // ---------------------------------------------------
    
    private static final String URL_PREFIX = "https://answers.yahoo.com/question/index?qid=";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    private static CheckTitle ct = new CheckTitle();
    private static CheckBody cb = new CheckBody();
    private static CheckTopLevelCategory cc = new CheckTopLevelCategory();
    private static CheckBestAnswer cba = new CheckBestAnswer(); // not implemented

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: GetYAnswersPropertiesFromQid <plaintext-list-of-qids> <out-file>");
            return;
        }
        
        BufferedWriter writer = getWriter(args[1]);        
        String[] qids = getQids(args[0]);

        for (String qid : qids) {
            System.out.println("Getting data for QID " + qid);
            writer.append(qid + ":");
            writer.newLine();
            for (Entry<String, String> kv : extractData(qid).entrySet()) {
                writer.append(kv.getKey() + "\t" + kv.getValue());
                writer.newLine();
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private static String[] getQids(String inFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(inFile)));
        List<String> qids = new ArrayList<>();
        String l = reader.readLine();
        while (l != null) {
            qids.add(l);
            l = reader.readLine();
        }
        reader.close();
        return qids.toArray(new String[qids.size()]);
    }

    private static BufferedWriter getWriter(String fileLocation) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileLocation))));
    }

    /**
     * 
     * @param iQid question ID
     * @return map of features and attributes: question title, body, category, best answer, date
     * @throws Exception
     */
    public static Map<String, String> extractData(String iQid) throws Exception {

        Map<String, String> res = new HashMap<>();

        // parse date from qid
        res.put("Date", DATE_FORMAT.parse(iQid.substring(0, 14)).toString());

        // get and mine html page
        String url = URL_PREFIX + iQid;
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream responseBody = method.getResponseBodyAsStream();

            // strip top levels
            Document doc = Jsoup.parse(responseBody, "UTF8", url);
            Element html = doc.child(0);
            
            Element body = html.child(1);
            Element head = html.child(0);

            // get category
            res.put("Top level Category", findElementText(body, cc));

            // get title
            res.put("Title", findElementText(head, ct));

            // get body
            res.put("Body", findElementText(head, cb));

            // get best answer
            // res.put("Best Answer", findElementText(body, cba));

            responseBody.close();

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }

        return res;
    }

    private static String findElementText(Element topElem, ElementPredicate f) {
        Element elem = findElement(topElem, f);
        return elem == null ? "" : f.getText(elem);
    }

    private static Element findElement(Element e, ElementPredicate f) {
        if (f.check(e)) {
            return e;
        }
        for (Element c : e.children()) {
            Element elem = findElement(c, f);
            if (elem != null) {
                return elem;
            }
        }
        return null;
    }

}
