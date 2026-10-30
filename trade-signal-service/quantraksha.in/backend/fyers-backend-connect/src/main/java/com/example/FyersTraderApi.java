package com.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.tts.in.model.StockHistoryModel;
import com.tts.in.utilities.Tuple;
import kotlin.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tts.in.model.FyersClass;
import com.tts.in.websocket.FyersSocket;
import com.tts.in.websocket.FyersSocketDelegate;

import in.tts.hsjavalib.ChannelModes;

public class FyersTraderApi implements FyersSocketDelegate {

    private static FyersTraderApi fyersTraderApi;

    private  static FyersClass fyersClass;

    private final static String GENERIC_MESSAGE = "CHeck foundation setup";

    public static FyersTraderApi getInstance() {
       if(fyersTraderApi == null || fyersClass == null) {
           fyersTraderApi = new FyersTraderApi();
           fyersTraderApi.init();
       }
        return  fyersTraderApi;
    }

    public static void main(String[] args) {
        /*String clientID = "M0R4WW1PYU-100";
        String LiveToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhcGkuZnllcnMuaW4iLCJpYXQiOjE3Mjc3NTUwMzEsImV4cCI6MTcyNzgyOTAxMSwibmJmIjoxNzI3NzU1MDMxLCJhdWQiOlsieDowIiwieDoxIiwieDoyIiwiZDoxIiwiZDoyIiwieDoxIiwieDowIl0sInN1YiI6ImFjY2Vzc190b2tlbiIsImF0X2hhc2giOiJnQUFBQUFCbS0zTVh0bzRjY2dSTmxhS3NLUHZ5NDZYN1RsN09laW5oLVVfWjJOMkNGNEVyQUxqMFc0UG0zbTFXSVF5U1R4SmR2b28wWGEyNWpOZFhkd0JqcVBiekZMZWdfaGNqcUFQcGtJdGZPVlREdkNfX2xCZz0iLCJkaXNwbGF5X25hbWUiOiJLVU1BUiBLSVNIT1JFIEtVTUFSIiwib21zIjoiSzEiLCJoc21fa2V5IjoiMjQ5MmUyMGE1YjRkMDBkZmIxODQ4ZDgyNzExZGZmMmM4MWYzNTc5MmVmNDNjMmI4YTE0NWQyZmMiLCJmeV9pZCI6IllLMDQzOTEiLCJhcHBUeXBlIjoxMDAsInBvYV9mbGFnIjoiTiJ9.srlRU8-d55f3xzdTN5R3g4kr9tUFjboW_87ZIDU1Yqw";

        FyersClass fyersClass = FyersClass.getInstance();
        fyersClass.clientId = clientID;
        fyersClass.accessToken = LiveToken;
        App app = new App();
        app.WebSocket();*/
        FyersTraderApi app = new FyersTraderApi();
        app.init();

    }


    public void init() {
        System.out.println("Inir......");
        String clientID = "BTK62UP0KP-100";
        String secret = "TSUE8DAW23";
        String appHashId = "e5bda58b7bc3b0365376e909ef5a414c2db9f41917939f7f263ed0cf0447e29f";


        String redictUrl = "https://trade.fyers.in/api-login/redirect-uri/index.html";






        this.fyersClass = FyersClass.getInstance();
        this.fyersClass.clientId = clientID;

        FyersTraderApi app = new FyersTraderApi();
        if(!FileUtils.isFilePresent("/Users/sagar/developement/trading-project/trading-automation/backend-api/fyers_refresh_token.txt") ||
        FileUtils.isFileOlderThan24Hours("/Users/sagar/developement/trading-project/trading-automation/backend-api/fyers_refresh_token.txt") ) {
               app.GetGenerateCode(redictUrl, fyersClass);

            System.out.println("Please manually open the Fyers auth URL in your browser and get the code.");
            // Assuming you need to construct the URL, but since it's library, perhaps hardcode or find it.

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the auth code: ");
            String token = scanner.nextLine();

            System.out.println("Token:" + token);
            fyersClass.accessToken = token;

            app.GetGenerateToken(token, appHashId, fyersClass);
            this.WebSocket();

        } else {
            String existingToken = FileUtils.readFileAsString("/Users/sagar/developement/trading-project/trading-automation/backend-api/fyers_refresh_token.txt");
            fyersClass.accessToken =existingToken;
        }
        System.out.println("Item1: "+app.GetOptionChain().Item1());
        System.out.println("Item2: "+app.GetOptionChain().Item2());

//        app.GetProfile();
//        app.GetFunds();
//        app.GetHoldings();
//        app.GetStockHistory();
//        app.GetNifty5pIndexHistory("2025-08-06");

    }

    public Tuple<JSONObject, JSONObject> GetOptionChain() {
        String symbol = "NSE:NIFTY50-INDEX";
        int strikeCount = 50;
        String timestamp = "";

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetOptionChain(symbol, strikeCount, timestamp);

        return stockTuple;
    }

    public  Pair<Pojo.OrderEntry, Pojo.OrderEntry> GetNifty5pIndexHistoryForExpiryOptionBuy(String fromDate, String expiryDateOnly, String contractDate) {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = "NSE:NIFTY50-INDEX";
        model.Resolution = "30";
        model.DateFormat = "1";
        model.RangeFrom = fromDate;
        model.RangeTo = fromDate;
        model.ContFlag = 1;

        System.out.println("fromdate:" +fromDate);
        System.out.println("todate:"+fromDate);

//        Tuple<JSONObject, JSONObject> optionChain = this.GetOptionChain();
//        if (optionChain.Item2() == null) {
//            System.out.println("OptionChain:" + optionChain.Item1());
//        } else {
//            System.out.println("OptionChain Error: " + optionChain.Item2());
//        }



        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        JSONArray candles = (JSONArray) stockTuple.Item1().get("candles");

        System.out.println("Candles:"+candles);
        Pojo.HighLow highLow = GetResultHighAndLow(candles);

        /*Calculate Strike for Call and Put Options for Nifty50 Indices*/
        Pojo.CallPutStrikes callPutStrikes = calculateCallPutStrike(highLow);
        System.out.println("Call Option callPutStrikes:"+callPutStrikes);


        String expiryDate = null;

        if(contractDate !=null || !contractDate.isEmpty()) {
            expiryDate = contractDate;
        } else {
            expiryDate = expiryDateOnly;
        }


        /*Calculation for Call Strike Price*/
        String finalStrikeCE = getFinalStrike(""+callPutStrikes.callStrike(), expiryDate, "CE");
        Pojo.OrderEntry orderEntryCallOption = calculateForOptionContract(fyersClass, finalStrikeCE, fromDate);

        System.out.println("Call Option:"+orderEntryCallOption+", "+finalStrikeCE);


        /*Calculation for Put Strike Price*/
        String finalStrikePE = getFinalStrike(""+callPutStrikes.putStrike(), expiryDate, "PE");

        Pojo.OrderEntry orderEntryPutOption = calculateForOptionContract(fyersClass, finalStrikePE, fromDate);

        System.out.println("orderEntryCallOption:"+orderEntryCallOption);
        System.out.println("orderEntryPutOption:"+orderEntryPutOption);

        return  new Pair<>(orderEntryPutOption, orderEntryCallOption);
    }

    public Pair<Pojo.OrderEntry, Pojo.OrderEntry> GetNifty5pIndexHistoryForExpiryOptionSell(String fromDate, String toDate, String expiryDateOnly) {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = "NSE:NIFTY50-INDEX";
        model.Resolution = "1D";
        model.DateFormat = "1";
        model.RangeFrom = fromDate;
        model.RangeTo = toDate;
        model.ContFlag = 1;

        System.out.println("fromdate:" +fromDate);
        System.out.println("todate:"+fromDate);

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        JSONArray candles = (JSONArray) stockTuple.Item1().get("candles");

        System.out.println("Candles:"+candles);
        //Pair<OrderEntry> tets = new Pair();
        return null;
    }


    public Pojo.CallPutStrikes calculateCallPutStrike(Pojo.HighLow highLow) {
        System.out.println("highLow "+highLow);
        Double ceStrike =   highLow.maxHigh() *(1+ 0.10/100);
        Double peStrike =  highLow.minLow() * (1- 0.10/100) ;

        int roundUpValue  = Math.abs(Math.ceilMod(ceStrike.longValue(), 50));
        int roundDownValue = (Math.floorMod(peStrike.longValue(), 50));



        Double finalStrikeCE = ceStrike + roundUpValue;
        Double finalStrikePE = peStrike - roundDownValue;

        Pojo.CallPutStrikes callPutStrikes = new Pojo.CallPutStrikes(finalStrikeCE.intValue(), finalStrikePE.intValue());
        return  callPutStrikes;
    }

    public Pojo.HighLow GetResultHighAndLow(JSONArray candles) {
        int counter = 0;
        Double maximumHigh = 0.0;
        Double maximumLow = 0.0 ;

        for (int i=0; i < candles.length(); i++){
            counter++;
            JSONArray candle = candles.getJSONArray(i);
            if(counter >= 11 ) {


               // System.out.println(" High "+ candle.get(2).toString() + ", Low "+candle.get(3).toString());

                Double candleHigh = Double.valueOf( candle.get(2).toString());
                Double candleLow = Double.valueOf( candle.get(3).toString());



                if(maximumHigh == 0 || candleHigh >= maximumHigh) {
                    maximumHigh = candleHigh;
                }
                if(maximumLow == 0 || candleLow <= maximumLow) {
                    maximumLow = candleLow;
                }

            }

        }
        Pojo.HighLow highLow = new Pojo.HighLow(maximumHigh,  maximumLow);


        return highLow;

    }


    public String getFinalStrike(String finalStrikeCE, String expiryDate, String indicatorCallPut) {
        String market = "NSE:";
        String indice = "NIFTY";

        String strike = finalStrikeCE;


        //NIFTY25080724700CE

        String finalOptionContract = market+indice+expiryDate+strike+indicatorCallPut;
        System.out.println("Final Option Contract:"+finalOptionContract);
        return  finalOptionContract;
    }

    public Pojo.OrderEntry calculateForOptionContract(FyersClass fyersClass, String finalStrikeCE, String fromDate) {


        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = finalStrikeCE;
        model.Resolution = "30";
        model.DateFormat = "1";
        model.RangeFrom = fromDate;
        model.RangeTo = fromDate;
        model.ContFlag = 1;


        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        if (stockTuple.Item2() == null) {
           // System.out.println("Stock History: " + stockTuple.Item1());
        } else {
            //System.out.println("Stock History Error: " + stockTuple.Item2());
        }
        JSONArray candles = (JSONArray) stockTuple.Item1().get("candles");

        Pojo.HighLow highLow = GetResultHighAndLow(candles);

        OrderEntryCalculation orderEntryCalculationCE = new OrderEntryCalculation();
        Pojo.OrderEntry orderEntryOption = orderEntryCalculationCE.calculate(finalStrikeCE, highLow);

        return  orderEntryOption;

    }



    public void GetNifty5pIndexHistoryByEpoch(FyersClass fyersClass, String fromDate, String toDate) {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = "NSE:NIFTY50-INDEX";
        model.Resolution = "15";
        model.DateFormat = "0";
        model.RangeFrom = ""+NextWednesdayCalculator.dateStringToEpochSeconds(fromDate);
        model.RangeTo = ""+NextWednesdayCalculator.dateStringToEpochSeconds(toDate);
        model.ContFlag = 1;



        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        if (stockTuple.Item2() == null) {
            //System.out.println("Stock History: " + stockTuple.Item1());
        } else {
            //System.out.println("Stock History Error: " + stockTuple.Item2());
        }


    }



    public String GetProfile() {
        Tuple<JSONObject, JSONObject> ProfileResponseTuple = fyersClass.GetProfile();

        if (ProfileResponseTuple.Item2() == null) {
            return "Profile: " + ProfileResponseTuple.Item1();
        }
        return GENERIC_MESSAGE;
    }

    public String GetFunds() {
        Tuple<JSONObject, JSONObject> ResponseTuple = fyersClass.GetFunds();

        if (ResponseTuple.Item2() == null) {
            return "Fund: " + ResponseTuple.Item1();
        }
        return GENERIC_MESSAGE;
    }

    public void GetStockHistory() {
        StockHistoryModel model = new StockHistoryModel();
        model.Symbol = "NSE:SBIN-EQ";
        model.Resolution = "30";
        model.DateFormat = "1";
        model.RangeFrom = "2025-07-01";
        model.RangeTo = "2025-07-25";
        model.ContFlag = 1;

        Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);
        if (stockTuple.Item2() == null) {
            //System.out.println("Stock History: " + stockTuple.Item1());
        } else {
            //System.out.println("Stock History Error: " + stockTuple.Item2());
        }
    }

    public String GetHoldings() {
        Tuple<JSONObject, JSONObject> holdingTuple = fyersClass.GetHoldings();
        if (holdingTuple.Item2() == null) {
            return  "Holdings: " + holdingTuple.Item1();
        }
        return GENERIC_MESSAGE;
    }

    public void GetGenerateCode(String redirectURI, FyersClass fyersClass) {
        fyersClass.GenerateCode(redirectURI);
    }

    public void GetGenerateToken(String code, String appHashId, FyersClass fyersClass) {

        String token="";

        try {

               JSONObject jsonObject = fyersClass.GenerateToken(code, appHashId);
               fyersClass.accessToken = jsonObject.getString("access_token");
               FileUtils.saveString("fyers_access_token.txt", fyersClass.accessToken);
               FileUtils.saveString("fyers_refresh_token.txt", fyersClass.accessToken);


        } catch (Exception ex){

        }
        fyersClass.accessToken = token;
    }

    public  void saveString(String filename, String content) throws IOException {
        Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
    }

    public void WebSocket()  {
        List<String> scripList = new ArrayList<>();
       // scripList.add("NSE:SBIN-EQ");
        scripList.add("NSE:NIFTY50-INDEX");
        FyersSocket fyersSocket = new FyersSocket(60);
        fyersSocket.webSocketDelegate = this;
        // fyersSocket.Connect();

         fyersSocket.ReconnectAttemptsCount = 5;

         try {
             Thread.sleep(100L);
         } catch (Exception ex){

         }
         //fyersSocket.OnOpen("Connection is Open");
        fyersSocket.ConnectHSM(ChannelModes.LITE);
        try {
        Thread.sleep(100L);
    } catch (Exception ex){

    }
        //
        //fyersSocket.
        fyersSocket.SubscribeData(scripList);


    }

    @Override
    public void OnIndex(JSONObject index) {
        System.out.println("On Index: " + index);
    }

    @Override
    public void OnScrips(JSONObject scrips) {
        System.out.println("On Scrips: " + scrips);
    }

    @Override
    public void OnDepth(JSONObject depths) {
        System.out.println("On Depth: " + depths);
    }

    @Override
    public void OnOrder(JSONObject orders) {
        System.out.println("On Orders: " + orders);
    }

    @Override
    public void OnTrade(JSONObject trades) {
        System.out.println("On Trades: " + trades);
    }

    @Override
    public void OnPosition(JSONObject positions) {
        System.out.println("On Positions: " + positions);
    }

    @Override
    public void OnOpen(String status) {
        System.out.println("On open: " + status);
    }

    @Override
    public void OnClose(String status) {
        System.out.println("On Close: " + status);
    }

    @Override
    public void OnError(JSONObject error) {
        System.out.println("On Error: " + error);
    }

    @Override
    public void OnMessage(JSONObject message) {
        System.out.println("OnMessage: " + message);
    }

}