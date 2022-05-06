package com.example.lukabaljak.elabcrowdsensing;


import com.example.lukabaljak.elabcrowdsensing.formatiranjedatuma.FormatiranjeDatuma;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;

public class Merenje {

    private double x, y, z, duzina, sirina;
    private GregorianCalendar datumIvreme;

    public Merenje(double x, double y, double z,
                   GregorianCalendar datumIvreme, double duzina, double sirina) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.datumIvreme = datumIvreme;
        this.duzina = duzina;
        this.sirina = sirina;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public GregorianCalendar getDatumIvreme() {
        return datumIvreme;
    }

    public void setDatumIvreme(GregorianCalendar datumIvreme) {
        this.datumIvreme = datumIvreme;
    }

    @Override
    public String toString() {
        return "Merenje{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", sekunde:" + datumIvreme.toString() +
                ", duzina: " + duzina +
                ", sirina: " + sirina +
                '}';
    }

    public JSONObject toJSONObject(int kontekstMerenja) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("vkid", kontekstMerenja);
            jsonObject.put("X", x);
            jsonObject.put("Y", y);
            jsonObject.put("Z", z);
            jsonObject.put("datumIvreme", FormatiranjeDatuma.dajString(datumIvreme));
            jsonObject.put("duzina", duzina);
            jsonObject.put("sirina", sirina);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


}