package com.pmeller.altairusefulscraper.service;

import com.pmeller.altairusefulscraper.Estate;
import com.pmeller.altairusefulscraper.repository.EstateRepository;
import com.pmeller.altairusefulscraper.scraper.AbstractEstateScraper;
import com.pmeller.altairusefulscraper.scraper.OlxEstateScraper;
import com.pmeller.altairusefulscraper.scraper.OtodomEstateScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class ScrapperService {

    @Autowired
    EstateRepository estateRepository;

    public void scrapOlx() {
        OlxEstateScraper olxEstateScraper =
                new OlxEstateScraper("https://www.olx.pl/nieruchomosci/domy/sprzedaz/poznan/");
        scrap(olxEstateScraper, OlxEstateScraper.SOURCE);
    }

    public void scrapOtodom() {
        OtodomEstateScraper otodomEstateScraper =
                new OtodomEstateScraper("https://www.otodom.pl/pl/oferty/sprzedaz/mieszkanie/poznan");
        scrap(otodomEstateScraper, OtodomEstateScraper.SOURCE);
    }

    private void scrap(AbstractEstateScraper scraper, String scraperName) {
        log.info(String.format("Running %s scraping", scraperName));
        Instant start = Instant.now();
        try {
            scraper.scrapLinks();
            for (Estate estate : scraper) {
                if (estate == null) {
                    continue;
                }
                estateRepository.save(estate);
                log.debug(estate.toString());
            }
            Instant end = Instant.now();
            log.info(String.format("%s scraping finished [%s]", scraperName, Duration.between(start, end)));
        } catch (IOException e) {
            Instant end = Instant.now();
            log.error(String.format("Error during %s scraping after [%s]", scraperName,
                    Duration.between(start, end)), e);
        }
    }
}
