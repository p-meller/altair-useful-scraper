package com.pmeller.altairusefulscraper.scraper;

import com.pmeller.altairusefulscraper.Estate;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OtodomEstateScraper extends AbstractEstateScraper {

    public static final String SOURCE = "Otodom";
    private final Pattern decimalPattern;
    private final Random random = new Random();

    Map<String, Estate> estateDetails;

    public OtodomEstateScraper(String url) {
        super(String.format("%s?limit=100", url));
        String decimalRegex = "\\d+\\.?\\d+";
        decimalPattern = Pattern.compile(decimalRegex);
        estateDetails = new HashMap<>();
    }

    @Override
    public void scrapLinks() throws IOException {
        String currentUrl = this.url;
        HashSet<String> linkSet = new HashSet<>();
        int page = 1;
        while (true) {
            Instant start = Instant.now();
            Document doc = Jsoup.connect(currentUrl).get();
            Instant stop = Instant.now();


            Element noResult = doc.selectFirst("div[data-cy=\"no-search-results\"]");
            if (noResult != null) {
                break;
            }

            Elements allOffers = doc.select("div[role=\"main\"]>div[data-cy=\"search.listing\"]");

            Elements details = allOffers.select("a[data-cy=\"listing-item-link\"]");
            List<Element> detailsFitlered = details.stream().filter(element -> element.select("p").stream().
                    noneMatch(paragraph -> paragraph.hasText() && paragraph.text().equals("Zapytaj o cenę"))).toList();

            for (Element detailElement : detailsFitlered) {
                String link = String.format("https://www.otodom.pl%s", detailElement.attr("href"));

                if (linkSet.contains(link)) {
                    continue;
                }
                linkSet.add(link);

                Estate estate = new Estate();
                estate.setSource(SOURCE);

                Element nameElement = detailElement.selectFirst("h3[data-cy=\"listing-item-title\"]");
                if (nameElement != null) {
                    estate.setName(nameElement.text());
                }
                estate.setPrice(getDecimalFromElement(detailElement, "p", " zł"));
                estate.setPricePerMeter(getDecimalFromElement(detailElement, "strong", " zł/m²"));
                estate.setArea(getDecimalFromElement(detailElement, "span", " m²"));

                estateDetails.put(link, estate);

                log.info(estate.toString());

            }

            currentUrl = String.format("%s&page=%d", this.url, page);
            page += 1;

            long sleepTime = (long) random.nextDouble(1, 2) * Duration.between(start, stop).toMillis();
            try {
                Thread.sleep(sleepTime);
                log.info(String.format("Sleep for %d milliseconds",sleepTime));
            } catch (InterruptedException e) {
                log.error("Error during sleep", e);
                Thread.currentThread().interrupt();
            }

        }
        this.links.addAll(linkSet);
    }

    @Override
    protected Estate scrapData(String url) throws IOException {
        return estateDetails.get(url);
    }

    private Double getDecimalFromElement(Element element, String cssQuery, String suffix) {
        Optional<Element> decimalElement =
                element.select(cssQuery).stream().filter(el -> el.hasText() && el.text().contains(suffix)).findFirst();
        if (decimalElement.isPresent()) {
            Matcher matcher = decimalPattern.matcher(decimalElement.get().text().replace(" ", ""));
            if (matcher.find()) {
                return Double.parseDouble(matcher.group());
            }
        }
        return null;
    }
}
