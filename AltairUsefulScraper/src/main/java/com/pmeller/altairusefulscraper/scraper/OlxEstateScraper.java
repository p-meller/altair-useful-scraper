package com.pmeller.altairusefulscraper.scraper;

import com.pmeller.altairusefulscraper.Estate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OlxEstateScraper extends AbstractEstateScraper {

    public static final String SOURCE = "OLX";
    private final Pattern decimalPattern;

    public OlxEstateScraper(String url) {
        super(url);
        String decimalRegex = "\\d+\\.?\\d+";
        decimalPattern = Pattern.compile(decimalRegex);
    }

    public void scrapLinks() throws IOException {
        String currentUrl = this.url;
        HashSet<String> linkSet = new HashSet<>();
        while (true) {
            Document doc = Jsoup.connect(currentUrl).get();
            Elements offerList = doc.select("a.detailsLinkPromoted,a.detailsLink");

            List<String> offerLinks = offerList.stream().filter(element -> {
                String link = element.attr("href");
                return link.startsWith("https://www.olx.pl/");
            }).map(element -> element.attr("href")).toList();

            linkSet.addAll(offerLinks);

            Element nextPage = doc.selectFirst("a[data-cy=page-link-next]");

            if (nextPage != null) {
                currentUrl = nextPage.attr("href");
            } else {
                break;
            }
        }
        this.links.addAll(linkSet);
    }

    protected Estate scrapData(String url) throws IOException {
        Instant start = Instant.now();
        Document doc = Jsoup.connect(url).get();
        Instant stop = Instant.now();

        Element priceElement = doc.selectFirst("div[data-testid=ad-price-container]");
        if (priceElement == null) {
            return null;
        }

        Estate estate = new Estate();
        estate.setSource(SOURCE);

        Element nameElement = doc.selectFirst("h1[data-cy=ad_title]");
        if (nameElement != null) {
            estate.setName(nameElement.text());
        }
        estate.setPrice(findPrice(priceElement));

        List<String> details = doc.select("li>p").eachText();

        estate.setPricePerMeter(getDecimalFromDetails(details, "cena za m"));
        estate.setArea(getDecimalFromDetails(details, "powierzchnia"));

        sleepForRandomizedDuration(Duration.between(start, stop));

        return estate;
    }

    private Double getDecimalFromDetails(@NonNull List<String> details, @NonNull String prefix) {
        //TODO: check if there is no more than one element.
        Optional<String> pricePerMeter = details.stream().filter(s -> s.toLowerCase().startsWith(prefix)).findAny();
        if (pricePerMeter.isPresent()) {
            Matcher matcher = decimalPattern.matcher(pricePerMeter.get());
            if (matcher.find()) {
                return Double.parseDouble(matcher.group());
            }
        }
        return null;
    }

    private Double findPrice(Element priceElement) {
        String priceString = priceElement.text().replace(" ", "");
        Matcher matcher = decimalPattern.matcher(priceString);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return null;
    }
}
