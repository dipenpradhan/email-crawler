package com.dipenpradhan.janacrawler;

/**
 * Created by dipenpradhan on 2/15/16.
 */
public class Main {

    public static void main(String... args) {

        if(args.length>0) {
            String url = args[0];
            if(!url.contains("http://")){
                System.err.println("Enter with http. Example: http://syr.edu");
            }else {
                Crawler crawler = new Crawler(url);
                crawler.beginCrawling();
            }
        }else{
            System.err.println("Enter a website to begin crawling. Example: http://syr.edu");
        }
    }


}

