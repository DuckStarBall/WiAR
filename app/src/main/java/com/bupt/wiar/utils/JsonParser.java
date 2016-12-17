package com.bupt.wiar.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by JohnVenn on 2016/4/27.
 */
public class JsonParser {

    private Date[] dates;
    private double[] traffics;

    public JsonParser(String s) throws JSONException {

        JSONArray jsonArray = new JSONArray(s);
        int N = jsonArray.length();
        dates = new Date[N];
        traffics = new double[N];
        for (int i = 0; i < N; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            dates[N - 1 - i] = new Date(jsonObject.getLong("monTime") * 1000);
            traffics[N - 1 - i] = jsonObject.getInt("traffic");
        }

    }

    public Date[] getDates() {
        return dates;
    }

    public double[] getTraffics() {
        return traffics;
    }
}
