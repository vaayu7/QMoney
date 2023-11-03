
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  RestTemplate restTemplate;
  //StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  
// Wrapper function for parallel execution


public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
  PortfolioTrade trade, Double buyPrice, Double sellPrice) {

  Double days = (double)ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
  Double total_num_years = days/365;

  Double totalReturn = (sellPrice - buyPrice)/buyPrice;
  Double annualized_returns = Math.pow(1+ totalReturn, 1/total_num_years) - 1;

  return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
}





  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF
  // protected PortfolioManagerImpl(RestTemplate restTemplate) {
  //   this.restTemplate = restTemplate;
  // }








  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String url = buildUri(symbol, from, to);
        //RestTemplate restTemplate = new RestTemplate();
        List<Candle> ListCandles= new ArrayList<Candle>();

        TiingoCandle[] obj = restTemplate.getForObject(url,TiingoCandle[].class);
        for(TiingoCandle c : obj){
          ListCandles.add(c);
        }


      return ListCandles;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
        String uriTemplate = "https:api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate +"&endDate="+endDate+"&token=d62c1cebcf6bdd88429fb2d140e7ca722184d81a";
                             //"https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token=277efa2a65776a6cd53ee3f4b2b58b7012aa027e"
        return uriTemplate;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException {
        List<AnnualizedReturn> returns = new ArrayList<AnnualizedReturn>();
        //List<Candle> quotes = new ArrayList<Candle>();
    
        for (PortfolioTrade stock: portfolioTrades){
          LocalDate startDate=stock.getPurchaseDate();
          String symbol=stock.getSymbol();
          
          List<Candle> obj = getStockQuote(symbol, startDate, endDate);
          if(obj!=null){
            Double buyPrice=obj.get(0).getOpen();
            Double sellPrice=obj.get(obj.size()-1).getClose();
            returns.add(calculateAnnualizedReturns(endDate, stock, buyPrice, sellPrice));
          }
        }
        Collections.sort(returns, AnnualizedReturn.comparing);
        return returns;
    // TODO Auto-generated method stub
    //return null;
  }



}
