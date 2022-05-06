package com.example.lukabaljak.elabcrowdsensing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Common {

    private static final String DB_NAME = "DB_NAME";
    private static final String COLLECTION_NAME = "COLLECTION_NAME";
    private static final String API_KEY = "API_KEY";

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }
   /*
    public static String getAddressSingle(RezultatiMerenja rezultatiMerenja){
        String baseUrl = String.format("https://api.mlab.com/api/1/databases/%s/collections/%s",
                DB_NAME, COLLECTION_NAME);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("/"+rezultatiMerenja.getId().getoId()+"?apiKey="+API_KEY);

        return stringBuilder.toString();
    }*/

    public static String getPOSTAddress() {
        String baseUrl = String.format("https://api.mlab.com/api/1/databases/%s/collections/%s",
                DB_NAME, COLLECTION_NAME);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("?apiKey=" + API_KEY);
        return stringBuilder.toString();
    }

    public static String getMACAdressJSON(String MACAdress) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MACAdress", MACAdress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    /*
    public static String dajListuMerenjaUJSONu(RezultatMerenja rezultatiMerenja) {

        JSONObject set = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Merenje merenje : rezultatiMerenja.getMerenja()) {
            jsonArray.put(merenje.toJSONObject());
        }
        try {
            jsonObject.put(rezultatiMerenja.getKontekst(), jsonArray);
            //Ovo moze i da se izbaci. Tako treba na MLABU xD
            set.put("$set", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return set.toString();
    }*/

    public static String getAddressSingle(RezultatMerenja rezultatiMerenja) {
        String baseUrl = String.format("API_URL",
                DB_NAME, COLLECTION_NAME);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("/" + rezultatiMerenja.getId().getoId() + "?apiKey=" + API_KEY);

        return stringBuilder.toString();
    }

    public static String getSingInJSON(String macAddress, String imePrezime, String brojIndeksa) {
        JSONObject singinJSON = new JSONObject();
        try {
            singinJSON.put("macAddress", macAddress);
            singinJSON.put("imePrezime", imePrezime);
            singinJSON.put("brojIndeksa", brojIndeksa);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return singinJSON.toString();
    }


    public static String dajBoljuListuMerenjaUJSONU(RezultatMerenja rezultatiMerenja,int kontekstMerenja) {

        //JSONObject set = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Merenje merenje : rezultatiMerenja.getMerenja()) {
            jsonArray.put(merenje.toJSONObject(kontekstMerenja));
        }
        try {
            jsonObject.put("merenja", jsonArray);
            //Ovo moze i da se izbaci. Tako treba na MLABU xD
            //set.put("merenja", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray.toString();
    }

    public static String dajCeoJSON(String macAddress, String glavnoVreme, String merenja, JSONArray fftX, JSONArray fftY, JSONArray fftZ ){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MACAdress", macAddress);
            jsonObject.put("glavnoVreme", glavnoVreme);
            JSONArray jsonArray = new JSONArray(merenja);
            jsonObject.put( "merenja",jsonArray);

            jsonObject.put("analizaX", fftX);
            jsonObject.put("analizaY", fftY);
            jsonObject.put("analizaZ", fftZ);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }



}
