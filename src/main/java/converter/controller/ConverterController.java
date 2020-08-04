package converter.controller;

import converter.api.responses.ResponsePlatformApi;
import converter.services.interfaces.ConverterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/converter/")
public class ConverterController {

    private final ConverterService converterService;

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @GetMapping ("convert")
    public ResponseEntity<ResponsePlatformApi> converting(@RequestParam String currencyFrom,
                                                          @RequestParam String currencyTo,
                                                          @RequestParam double exchangeSum) {
        return converterService.converting(currencyFrom, currencyTo, exchangeSum);
    }

    @GetMapping ("history")
    public ResponseEntity<ResponsePlatformApi> getAllHistory(@RequestParam(defaultValue = "0") int offset,
                                                             @RequestParam(defaultValue = "10") int limit) {
        return converterService.getAllHistory(offset, limit);
    }

    @GetMapping ("history/date")
    public ResponseEntity<ResponsePlatformApi> getHistoryByDate(@RequestParam(defaultValue = "0") int offset,
                                                                @RequestParam(defaultValue = "10") int limit,
                                                                @RequestParam String date) {
        return converterService.getHistoryByDate(offset, limit, date);
    }
}
