package org.trec.liveqa;

import java.io.IOException;

/**
 * Copyright 2015 Yahoo Inc.<br>
 * Licensed under the terms of the MIT license. Please see LICENSE file at the root of this project for terms.
 * <p/>
 * 
 * This class induces some test behaviors based on QID contents: failure, short delay, disqualifying delay. Based on title length,
 * answer might also become too long.
 * 
 * @author yuvalp@yahoo-inc.com
 * 
 */
public class SomeUseCasesServer extends TrecLiveQaDemoServer {

    public static final String WEASEL_TEXT =
                    " is not the real question. The real question is who will keep our country safe.";

    public SomeUseCasesServer(int port) {
        super(port);
    }

    @Override
    protected AnswerAndResourcesAndSummaries getAnswerAndResourcesAndSummaries(String qid, String title, String body, String category)
                    throws InterruptedException {
        if (qid.contains("F")) {
            // "random failure"
            return null;
        }
        if (qid.contains("D")) {
            // "random delay"
            Thread.sleep(1000);
        } else if (qid.contains("X")) {
            // "random massive delay"
            Thread.sleep(100000);
        }

        StringBuilder resourceSb = new StringBuilder();
        for (String catWord : category.split("\\s+")) {
            resourceSb.append(catWord + RESOURCES_LIST_SEPARATOR);
        }
        resourceSb.append(title.length() + body.length());

        return new AnswerAndResourcesAndSummaries(title + WEASEL_TEXT, resourceSb.toString(), "", "", "");
    }

    public static void main(String[] args) throws IOException {
        SomeUseCasesServer server = new SomeUseCasesServer(args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]));
        server.start();
        System.in.read();
        server.stop();
    }

}
