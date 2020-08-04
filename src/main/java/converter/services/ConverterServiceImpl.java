package converter.services;

import converter.api.responses.RequestsResponseBody;
import converter.api.responses.ResponsePlatformApi;
import converter.model.History;
import converter.model.Rate;
import converter.repositories.CurrencyRepository;
import converter.repositories.HistoryRepository;
import converter.repositories.RateRepository;
import converter.services.interfaces.AuthService;
import converter.services.interfaces.ConverterService;
import converter.services.interfaces.ParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ConverterServiceImpl implements ConverterService {

    @Value("${badRequest.message}")
    private String badRequestMessage;

    private final AuthService authService;
    private final ParserService parserService;
    private final RateRepository rateRepository;
    private final CurrencyRepository currencyRepository;
    private final HistoryRepository historyRepository;

    public ConverterServiceImpl(AuthService authService, ParserService parserService, RateRepository rateRepository,
                                CurrencyRepository currencyRepository, HistoryRepository historyReposirory) {
        this.authService = authService;
        this.parserService = parserService;
        this.rateRepository = rateRepository;
        this.currencyRepository = currencyRepository;
        this.historyRepository = historyReposirory;
    }

    @Override
    public ResponseEntity<ResponsePlatformApi> converting(String currencyFrom, String currencyTo, double exchangeSum) {

        double gettingSum = 0.0;
        int rateId = 0;

        if (!authService.isUserAuthorize())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        /**Это защита для обращений напрямую через API, где можно написать любой несуществующий код валюты */

        boolean isOneOfRuble = isOneOfRuble(currencyFrom, currencyTo);
        boolean isBothCurrenciesNotExist = isCurrencyNotExist(currencyFrom) || isCurrencyNotExist(currencyTo);
        boolean currencyFromIsRub = currencyFrom.equalsIgnoreCase("RUB");
        boolean currencyToIsRub = currencyTo.equalsIgnoreCase("RUB");
        boolean currencyToNotExist = isCurrencyNotExist(currencyTo);
        boolean currencyFromNotExist = isCurrencyNotExist(currencyFrom);

        if ((!isOneOfRuble && isBothCurrenciesNotExist) || (currencyFromIsRub && currencyToNotExist) ||
                (currencyToIsRub && currencyFromNotExist) || (currencyFrom.equals(currencyTo)))
        {
            log.info("Currency from: '{}' and Currency To: '{}'", currencyFrom, currencyTo);
            return ResponseEntity.badRequest().body(ResponsePlatformApi.builder()
                    .message(badRequestMessage).build());
        }

        try {
            parserService.initParseXML();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error(e.getMessage());
        }

        if (isOneOfRuble(currencyFrom, currencyTo)) {
            Rate curToConvert = rateRepository.findByCurrencyCodeAndDate((currencyFromIsRub) ? currencyTo : currencyFrom,
                                                                                             LocalDate.now());
            rateId = curToConvert.getId();

            int nominal = curToConvert.getNominal();
            double rate = curToConvert.getCurrencyRate();

            gettingSum = roundGettingSum((currencyFromIsRub) ? (exchangeSum * nominal) / rate :
                                                               (exchangeSum * rate) / nominal);
        }

        if (!isOneOfRuble) {
            Rate from = rateRepository.findByCurrencyCodeAndDate(currencyFrom, LocalDate.now());
            Rate to = rateRepository.findByCurrencyCodeAndDate(currencyTo, LocalDate.now());
            rateId = from.getId();
            int nominalFrom = from.getNominal();
            int nominalTo = to.getNominal();
            double rateFrom = from.getCurrencyRate();
            double rateTo = to.getCurrencyRate();

            double convertToRub = (exchangeSum * rateFrom) / nominalFrom;
            gettingSum = roundGettingSum((convertToRub * nominalTo) / rateTo);
        }

        int id = historyRepository.save(History.builder()
                .currencyFrom(currencyFrom)
                .currencyTo(currencyTo)
                .date(LocalDate.now())
                .exchangeSum(exchangeSum)
                .gettingSum(gettingSum)
                .userId(authService.getAuthorizedUserId())
                .rateId(rateId)
                .build()).getId();

        RequestsResponseBody responseBody = RequestsResponseBody.builder()
                .id(id)
                .currencyFrom(currencyFrom)
                .currencyTo(currencyTo)
                .exchangeSum(exchangeSum)
                .gettingSum(gettingSum)
                .date(LocalDate.now())
                .build();

        return ResponseEntity.ok(ResponsePlatformApi.builder().request(responseBody).build());
    }

    @Override
    public ResponseEntity<ResponsePlatformApi> getAllHistory(int offset, int limit) {

        if (authService.isUserAuthorize()) {
            int userId = authService.getAuthorizedUserId();
            int page = offset/limit;
            Pageable pageable = PageRequest.of(page, limit, Sort.by("date").descending());
            List<History> historyList = historyRepository.findByUserId(userId, pageable);
            int count = historyRepository.getHistoryCountByUser(userId);

            List<RequestsResponseBody> requests = createHistoryResponseBody(historyList);

            return new ResponseEntity<>(ResponsePlatformApi.builder().count(count)
                    .converterRequests(requests).build(), HttpStatus.OK);
        }
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Override
    public ResponseEntity<ResponsePlatformApi> getHistoryByDate(int offset, int limit, String date) {

        if (authService.isUserAuthorize()) {
            int userId = authService.getAuthorizedUserId();
            int page = offset/limit;
            LocalDate requestedDate = LocalDate.parse(date);

            Pageable pageable = PageRequest.of(page, limit, Sort.by("currencyFrom"));
            List<History> historyList = historyRepository.findByUserIdAndDate(userId, requestedDate, pageable);
            int count = historyRepository.getHistoryCountByUserAndDate(userId, requestedDate);

            List<RequestsResponseBody> requests = createHistoryResponseBody(historyList);

            return new ResponseEntity<>(ResponsePlatformApi.builder().count(count)
                    .converterRequests(requests).build(), HttpStatus.OK);
        }
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private List<RequestsResponseBody> createHistoryResponseBody(List<History> historyList) {
        List<RequestsResponseBody> requests = new ArrayList<>();

        for (History history: historyList) {

            requests.add(RequestsResponseBody.builder()
                    .id(history.getId())
                    .currencyFrom(history.getCurrencyFrom())
                    .currencyTo(history.getCurrencyTo())
                    .exchangeSum(history.getExchangeSum())
                    .gettingSum(history.getGettingSum())
                    .date(history.getDate())
                    .build());
        }
        return requests;
    }

    private boolean isCurrencyNotExist(String currency) {
        return currencyRepository.findByCharCode(currency) == null;
    }

    private double roundGettingSum(double gettingSum)
    {
        String result = String.format("%.3f",gettingSum);
        return Double.parseDouble(result.replace(",", "."));
    }

    private boolean isOneOfRuble (String currencyFrom, String currencyTo) {
        return currencyFrom.equalsIgnoreCase("RUB") || currencyTo.equalsIgnoreCase("RUB");
    }
}
