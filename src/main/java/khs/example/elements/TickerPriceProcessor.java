package khs.example.elements;

import java.math.BigDecimal;
import java.util.Currency;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
@Component("tickerPriceProcessor")
public class TickerPriceProcessor implements ItemProcessor<TickerData, TickerData> {
 
    @Autowired
    private CurrencyConversionService conversionService;
 

    public TickerData process(TickerData ticker) throws Exception {
 
        BigDecimal openGBP =  new BigDecimal("1000.00"); // conversionService.convertCurrency(ticker.getOpen(), Currency.USD, Currency.GBP);
        BigDecimal lastTradeGBP =    new BigDecimal("2000.00"); //conversionService.convertCurrency(ticker.getLastTrade(), Currency.USD, Currency.GBP);
 
        ticker.setOpenGBP(openGBP);
        ticker.setLastTradeGBP(lastTradeGBP);
 
        return ticker;
    }
 
}