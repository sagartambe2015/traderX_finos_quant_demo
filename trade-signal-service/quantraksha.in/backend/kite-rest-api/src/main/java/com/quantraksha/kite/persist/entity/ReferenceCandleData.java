package com.quantraksha.kite.persist.entity;

import org.json.JSONObject;
/**Objective: This class is used to store the reference candle data for a trade capyur.
 *
 * @author : sagar tambe
 */
public class ReferenceCandleData {
    public int referenceCandleDataId;
    private String instrumentId;
    private String interval;
    private String candleData;
    private String startDate;
    private String endDate;
    private JSONObject candleDataJson;

}
