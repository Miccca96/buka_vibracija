package com.example.lukabaljak.elabcrowdsensing.formatiranjedatuma;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class FormatiranjeDatuma {

    public static GregorianCalendar dajGregorianCalendar(String stringDatum){
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy. h:m:s");
        Date date = null;
        try {
            date = df.parse(stringDatum);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        GregorianCalendar datumIVreme = new GregorianCalendar();
        datumIVreme.setTime(date);
        //zato sto u GC brojanje krece od 0, a u Date od 1 lol
        datumIVreme.set(GregorianCalendar.MONTH, datumIVreme.get(GregorianCalendar.MONTH));
     return datumIVreme;
    }

    public static String dajString(GregorianCalendar gregorianCalendarDatum){
        return gregorianCalendarDatum.get(GregorianCalendar.DAY_OF_MONTH)+"."+
                gregorianCalendarDatum.get(GregorianCalendar.MONTH)+"."+
                gregorianCalendarDatum.get(GregorianCalendar.YEAR)+". "+
                gregorianCalendarDatum.get(GregorianCalendar.HOUR)+":"+
                gregorianCalendarDatum.get(GregorianCalendar.MINUTE)+":"+
                gregorianCalendarDatum.get(GregorianCalendar.SECOND);
    }

}
