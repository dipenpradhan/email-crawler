package com.dipenpradhan.emailcrawler;

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

    private static final String[] IGNORED_EXTENSIONS = new String[]{".pdf", ".jpg", ".bmp", ".png",
            ".doc", ".ppt", ".mov", ".3gp", ".mpg", ".mkv"};
    private static final short THREAD_COUNT = 50;

    private String startUrl;
    private String domain;
    private Set<String> emailSet = new HashSet<>();
    private Set<String> urlSet = new HashSet<>();
    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private File urlsFile, emailsFile;
    private PrintWriter urlsWriter, emailsWriter;
    private Pattern emailPattern = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Instantiates a new Crawler.
     *
     * @param domain the domain
     */
    public Crawler(String domain) {
        this.domain = domain;
        this.startUrl = "http://" + domain;
        init();
    }

    /**
     * Create files and initialize writers
     **/
    private void init() {

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

    /**
     * Begin crawling the page based on start domain provided
     */
    public void beginCrawling() {

        if (urlsFile != null
                && emailsFile != null
                && urlsWriter != null
                && emailsWriter != null) {
            crawl(startUrl);
        }
    }

    /**
     * Crawl this URL
     *
     * @param url the url
     */
    public void crawl(final String url) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                urlsWriter.println(url);
                urlsWriter.flush();

                urlSet.add(url); // Add it to main list of urls

                try {
                    Document doc = Jsoup.connect(url).get();     // Fetch the webpage, and create a JSoup Document object using it

                    findEmails(doc);        // Finds and processes new emails on this page

                    findNewUrls(doc);       // Finds new urls on this page and initiates crawling on them
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }

        };
        executor.execute(runnable);
    }


    /**
     * Find emails.
     *
     * @param doc the doc
     */
    private void findEmails(Document doc) {

        Matcher matcher = emailPattern.matcher(doc.body().toString());

        while (matcher.find()) {
            String email = matcher.group();
            if (emailSet.add(email)) {
                processNewEmail(email);
            }
        }
    }

    /**
     * Process new email.
     *
     * @param email the email
     */
    private void processNewEmail(String email) {
        emailsWriter.println(email);
        emailsWriter.flush();
        System.out.println(email);
    }

    /**
     * Find new urls set.
     *
     * @param doc the doc
     * @return the set
     */
    private Set<String> findNewUrls(Document doc) {
        Set<String> newUrlSet = new HashSet<>();

        for (Element ah : doc.select("a[href]")) {
            String href = ah.attr("abs:href");

            if (!urlSet.contains(href)              // Check if this is a new URL
                    && href.contains(domain)        // Check if the URL is from the same domain
                    && isValidExtension(href)       // Check that the file extension is not in the list of excluded extensions
                    && !href.contains("mailto:")    // Check that the href is not an email address
                    ) {
                newUrlSet.add(href);
            }

        }

        processNewUrls(newUrlSet);
        return newUrlSet;
    }

    /**
     * Process new urls.
     *
     * @param newUrls the new urls
     */
    private void processNewUrls(Set<String> newUrls) {
        for (String u : newUrls) {
            crawl(u);
        }
    }

    /**
     * Is the file extension valid. (checks IGNORED_EXTENSIONS list)
     */
    private boolean isValidExtension(String url){
        for (String ext : IGNORED_EXTENSIONS) {
            if (url.contains(ext)) {
                return false;
            }
        }
        return true;
    }
}
