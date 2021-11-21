package com.pmeller.altairusefulscraper.scraper;

import com.pmeller.altairusefulscraper.Estate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Slf4j
public abstract class AbstractEstateScraper implements Iterable<Estate> {

    protected String url;
    protected List<String> links;
    private final Random random = new Random();

    protected AbstractEstateScraper(String url){
        this.url = url;
        this.links = new ArrayList<>();
    }

    public abstract void scrapLinks() throws IOException;
    protected abstract Estate scrapData(String url) throws IOException;

    @Override
    public Iterator<Estate> iterator() {
        return new Iterator<>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return links != null && links.size() > currentIndex;
            }

            @Override
            public Estate next() {
                Estate estate = null;
                try {
                    estate = scrapData(links.get(currentIndex++));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return estate;
            }
        };
    }

    protected void sleepForRandomizedDuration(Duration duration){
        long sleepTime = (long) random.nextDouble(1, 2) * duration.toMillis();
        try {
            Thread.sleep(sleepTime);
            log.debug(String.format("Sleep for %d milliseconds",sleepTime));
        } catch (InterruptedException e) {
            log.error("Error during sleep", e);
            Thread.currentThread().interrupt();
        }
    }
}
