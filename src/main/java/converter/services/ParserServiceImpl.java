package converter.services;

import converter.model.Currency;
import converter.model.Rate;
import converter.repositories.CurrencyRepository;
import converter.repositories.RateRepository;
import converter.services.interfaces.ParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ParserServiceImpl implements ParserService {

    private final String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private final String URL = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + today;

    private final RateRepository rateRepository;
    private final CurrencyRepository currencyRepository;

    public ParserServiceImpl(RateRepository rateRepository, CurrencyRepository currencyRepository) {
        this.rateRepository = rateRepository;
        this.currencyRepository = currencyRepository;
    }

    @Bean
    public void initParseXML() {

        try {
            if (rateRepository.getRateCountByDate(LocalDate.now()) == 0) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLHandler handler = new XMLHandler();
                parser.parse(URL, handler);
                handler.writeToDb();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private class XMLHandler extends DefaultHandler {

        private Currency currency = null;
        private Rate rate = null;

        List<Currency> currencyList = new ArrayList<>();
        List<Rate> rateList = new ArrayList<>();

        boolean numCodeInd = false;
        boolean charCodeInd = false;
        boolean nameInd = false;
        boolean nominalInd = false;
        boolean valueInd = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            if (qName.equalsIgnoreCase("valute")) {
                currency = new Currency();
                rate = new Rate();
                currency.setCurrencyId(attributes.getValue("ID"));
                rate.setDate(LocalDate.now());
            } else if (qName.equalsIgnoreCase("numcode")) {
                numCodeInd = true;
            } else if (qName.equalsIgnoreCase("charcode")) {
                charCodeInd = true;
            } else if (qName.equalsIgnoreCase("nominal")) {
                nominalInd = true;
            } else if (qName.equalsIgnoreCase("value")) {
                valueInd = true;
            } else if (qName.equalsIgnoreCase("name")) {
                nameInd = true;
            }
        }

        private void writeToDb() {
            currencyRepository.saveAll(currencyList);
            rateRepository.saveAll(rateList);

            log.info("В базу валют записано '{}' наименнований", currencyList.size());
            log.info("В базу курсов записано '{}' наименнований", rateList.size());
        }

        @Override
        public void characters(char[] ch, int start, int length) {

            String value = new String(ch, start, length);
            if (numCodeInd) {
                currency.setNumCode(value);
                numCodeInd = false;
            } else if (charCodeInd) {
                currency.setCharCode(value);
                rate.setCurrencyCode(value);
                charCodeInd = false;
            } else if (nameInd) {
                currency.setName(value);
                nameInd = false;
            } else if (nominalInd) {
                rate.setNominal(Integer.parseInt(value));
                nominalInd = false;
            } else if (valueInd) {
                rate.setCurrencyRate(Double.parseDouble(value.replace(',', '.')));
                valueInd = false;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase("valute")) {
                currencyList.add(currency);
                rateList.add(rate);
                currency = null;
                rate = null;
            }
        }
    }
}
