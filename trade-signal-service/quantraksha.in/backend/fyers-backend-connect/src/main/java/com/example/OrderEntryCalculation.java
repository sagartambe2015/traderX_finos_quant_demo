package com.example;

public class OrderEntryCalculation {


        public  OrderEntryCalculation (){

        }

        public Pojo.OrderEntry calculate(String strike, Pojo.HighLow highLow){
            System.out.println("Option High Low:"+highLow);
            double multiplier = 0.05;
            double entryPrice = MathUtils.mround(highLow.maxHigh() + (highLow.maxHigh() * 12 /100),
                    multiplier);

            double target  = MathUtils.mround( entryPrice + (entryPrice * 70/100), multiplier);
            double sl=  MathUtils.mround( entryPrice - (entryPrice * 70/100), multiplier);
            double tsl = MathUtils.mround( highLow.minLow() - (highLow.minLow() * 12/100), multiplier);
            Pojo.OrderEntry orderEntry = new Pojo.OrderEntry(strike,
                   entryPrice, target, sl, tsl );
            return  orderEntry;
        }



    }