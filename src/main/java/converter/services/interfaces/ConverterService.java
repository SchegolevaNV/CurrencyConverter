package converter.services.interfaces;

import converter.api.responses.ResponsePlatformApi;
import org.springframework.http.ResponseEntity;

public interface ConverterService {

    ResponseEntity<ResponsePlatformApi> converting(String currencyFrom, String currencyTo, double exchangeSum);
    ResponseEntity<ResponsePlatformApi> getAllHistory(int offset, int limit);
    ResponseEntity<ResponsePlatformApi> getHistoryByDate(int offset, int limit, String date);
}
