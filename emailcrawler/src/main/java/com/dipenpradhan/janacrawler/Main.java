package com.dipenpradhan.janacrawler;

/**
 * Created by dipenpradhan on 2/15/16.
 */
public class Main {

    public static void main(String... args) {

        if(args.length>0) {
            String domain = args[0];
            if(domain.contains("http://")){
                System.err.println("DO NOT enter with http. Enter only the domain name Example: syr.edu");
            }else {
                Crawler crawler = new Crawler(domain);
                crawler.beginCrawling();
            }
        }else{
            System.err.println("Enter a domain to begin crawling. Example: syr.edu");
        }
    }


}

