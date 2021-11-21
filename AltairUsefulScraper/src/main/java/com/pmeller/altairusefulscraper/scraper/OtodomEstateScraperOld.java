package com.pmeller.altairusefulscraper.scraper;

import com.pmeller.altairusefulscraper.Estate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated For now deprecated, because all useful information are on the list of offer.
 */
@Deprecated(since = "intial version")
public class OtodomEstateScraperOld extends AbstractEstateScraper {

    public static final String SOURCE = "Otodom";
    private final Pattern decimalPattern;


    public OtodomEstateScraperOld(String url) {
        super(String.format("%s?limit=100", url));
        String decimalRegex = "\\d+\\.?\\d+";
        decimalPattern = Pattern.compile(decimalRegex);
    }

    @Override
    public void scrapLinks() throws IOException {
        String currentUrl = this.url;
        HashSet<String> linkSet = new HashSet<>();
        int page = 1;
        while (true) {
            Document doc = Jsoup.connect(currentUrl).get();

            Element noResult = doc.selectFirst("div[data-cy=\"no-search-results\"]");
            if (noResult != null) {
                break;
            }

            Elements allOffers = doc.select("div[role=\"main\"]>div[data-cy=\"search.listing\"]");

            List<String> offerLinks = allOffers.select("a[data-cy=listing-item-link]").stream().map(element -> {
                String url = element.attr("href");
                return String.format("https://www.otodom.pl%s", url);
            }).toList();

            linkSet.addAll(offerLinks);

            currentUrl = String.format("%s&page=%d", this.url, page);
            page += 1;
        }
        this.links.addAll(linkSet);
    }

    @Override
    protected Estate scrapData(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Element priceElement = doc.selectFirst("strong[aria-label=Cena]");

        if (priceElement == null || priceElement.text().toLowerCase().startsWith("zapytaj")) {
            return null;
        }

        Estate estate = new Estate();
        estate.setSource(SOURCE);

        Element nameElement = doc.selectFirst("h1[data-cy=adPageAdTitle]");
        if (nameElement != null) {
            estate.setName(nameElement.text());
        }

        Matcher priceMatcher = decimalPattern.matcher(priceElement.text().replace(" ", ""));
        if (priceMatcher.find()) {
            estate.setPrice(Double.parseDouble(priceMatcher.group()));
        }

        Element pricePerMeterElement = doc.selectFirst("div[aria-label=Cena za metr kwadratowy]");
        if (pricePerMeterElement != null) {
            Matcher matcher = decimalPattern.matcher(pricePerMeterElement.text().replace(" ", ""));
            if (matcher.find()) {
                estate.setPricePerMeter(Double.parseDouble(matcher.group()));
            }
        }

        Element areaElement = doc.selectFirst("div[aria-label=Powierzchnia]>div:last-child");
        if (areaElement != null) {
            Matcher matcher = decimalPattern.matcher(areaElement.text().replace(",", "."));
            if (matcher.find()) {
                estate.setArea(Double.parseDouble(matcher.group()));
            }
        }

        return estate;
    }
}
