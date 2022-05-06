package com.example.lukabaljak.elabcrowdsensing;

import java.util.HashMap;
import java.util.Map;

public class KategorijeMerenja {

    static KategorijeMerenja single_instance;

    static HashMap<Integer, String> kategorije = new HashMap();

    public static KategorijeMerenja getInstance() {
        if (single_instance == null) {
            single_instance = new KategorijeMerenja();
            kategorije.put(1, "U autobusu");
            kategorije.put(2, "U tramvaju");
            kategorije.put(3, "U trolejbusu");
            kategorije.put(4, "U automobilu");
        }

        return single_instance;
    }

    public int count() {
        return kategorije.size();
    }

    public String getNazivKategorije(int kod) {
        return kategorije.get(kod).toString();
    }

    public int getIDKategorije(String naziv) {
        for (Map.Entry<Integer, String> entry : kategorije.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            if(naziv.equals(value))
                return key;
        }
        return Integer.MAX_VALUE;
    }

}
