package com.example.lukabaljak.elabcrowdsensing;

public class PamcenjeMerenja {

    RezultatMerenja rezultatiMerenja;
    Merenje poslednjeMerenje;

    public PamcenjeMerenja(RezultatMerenja rezultatiMerenja) {
        this.rezultatiMerenja = rezultatiMerenja;
    }

    public RezultatMerenja getRezultatiMerenja() {
        return rezultatiMerenja;
    }

    public void setRezultatiMerenja(RezultatMerenja rezultatiMerenja) {
        this.rezultatiMerenja = rezultatiMerenja;
    }

    public Merenje getPoslednjeMerenje() {
        return poslednjeMerenje;
    }

    public void setPoslednjeMerenje(Merenje poslednjeMerenje) {
        this.poslednjeMerenje = poslednjeMerenje;
    }

    public void onMeasureChanged(Merenje merenje) {
        if (poslednjeMerenje == null) {
            rezultatiMerenja.getMerenja().add(merenje);
            poslednjeMerenje = merenje;
            return;
        }
        long razlikaUSekundama = (merenje.getDatumIvreme().getTimeInMillis() -
                poslednjeMerenje.getDatumIvreme().getTimeInMillis()) / 1000;
        if (razlikaUSekundama >= 1) {
            rezultatiMerenja.getMerenja().add(merenje);
            poslednjeMerenje = merenje;
        }

    }


}


