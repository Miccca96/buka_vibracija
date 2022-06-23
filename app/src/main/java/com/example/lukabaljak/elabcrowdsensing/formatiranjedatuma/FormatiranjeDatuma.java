package com.example.lukabaljak.elabcrowdsensing.formatiranjedatuma;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FormatiranjeDatuma {

    //bespotrebno, ne zove se nigde
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

        datumIVreme.set(GregorianCalendar.MONTH, datumIVreme.get(GregorianCalendar.MONTH), 1);



     return datumIVreme;
    }


    public static String dajString(GregorianCalendar gregorianCalendarDatum){

        //jer datumi krecu od 0
gregorianCalendarDatum.add(GregorianCalendar.MONTH, 1);

        return gregorianCalendarDatum.get(GregorianCalendar.DAY_OF_MONTH)+"."+
                gregorianCalendarDatum.get(GregorianCalendar.MONTH)+"."+
                gregorianCalendarDatum.get(GregorianCalendar.YEAR)+". "+
                gregorianCalendarDatum.get(GregorianCalendar.HOUR)+":"+
                gregorianCalendarDatum.get(GregorianCalendar.MINUTE)+":"+
                gregorianCalendarDatum.get(GregorianCalendar.SECOND);


    }

}
