package com.dipenpradhan.janacrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dipenpradhan on 2/15/16.
 */
public class Crawler {

    public static final String[] IGNORED_EXTENSIONS = new String[]{".pdf", ".jpg", ".bmp", ".png",
            ".doc", ".ppt", ".mov", ".3gp", ".mpg", ".mkv"};
    private String startUrl;
    private String domain;
    private Set<String> emailSet = new HashSet<>();
    private Set<String> urlSet = new HashSet<>();
    private ExecutorService executor = Executors.newFixedThreadPool(50);
    private File urlsFile, emailsFile;
    private PrintWriter urlsWriter, emailsWriter;

    public Crawler(String domain) {
        this.domain=domain;
        this.startUrl = "http://"+domain;
        DateFormat dateFormatter = new SimpleDateFormat("YYYY_MM_dd-HH_mm_ss_SSS");
        String timeString = dateFormatter.format(new Date(System.currentTimeMillis()));
        urlsFile = new File("urls_" + timeString + ".txt");
        emailsFile = new File("emails_" + timeString + ".txt");

        try {
            urlsFile.createNewFile();
            emailsFile.createNewFile();
            urlsWriter = new PrintWriter(urlsFile.getPath(), "UTF-8");
            emailsWriter = new PrintWriter(emailsFile.getPath(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void beginCrawling() {

        if (urlsFile != null
                && emailsFile != null
                && urlsWriter != null
                && emailsWriter != null) {
            crawl(startUrl);
        }
    }

    public void crawl(final String url) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                urlsWriter.println(url);

                urlSet.add(url);
                Set<String> newUrlSet = new HashSet<>();

                try {
                    Document doc = Jsoup.connect(url).get();
                    Pattern p = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = p.matcher(doc.body().toString());
                    while(matcher.find()) {
                        String email = matcher.group().replace("mailto:","");
                        if (emailSet.add(email)) {
                            emailsWriter.println(email);
                            System.out.println(email);
                        }
                    }

                    for (Element ah : doc.select("a[href]")) {
                        String href = ah.attr("abs:href");
                        boolean ignore = false;
                        for (String ext : IGNORED_EXTENSIONS) {
                            if (href.contains(ext)) {
                                ignore = true;
                                break;
                            }
                        }

                        if (href.contains(domain)
                                && !ignore
                                ) {

                            if (!href.contains("mailto:") && !urlSet.contains(href)) {
                                newUrlSet.add(href);
                            }
                        }

                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                urlsWriter.flush();
                emailsWriter.flush();
                for (String u : newUrlSet) {
                    crawl(u);
                }
            }

        };
        executor.execute(runnable);
    }
}
