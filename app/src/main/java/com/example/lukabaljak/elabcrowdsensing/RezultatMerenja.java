package com.example.lukabaljak.elabcrowdsensing;

import java.util.List;

public class RezultatMerenja {

    Id id;
    List<Merenje> merenja;
    String kontekst;

    public String getKontekst() {
        return kontekst;
    }

    public void setKontekst(String kontekst) {
        this.kontekst = kontekst;
    }

    public RezultatMerenja(List<Merenje> merenja) {
        this.merenja = merenja;
    }

    public RezultatMerenja(Id id, List<Merenje> merenja) {
        this.id = id;
        this.merenja = merenja;
    }

    public RezultatMerenja(Id id) {
        this.id = id;

    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public List<Merenje> getMerenja() {
        return merenja;
    }

    public void setMerenja(List<Merenje> merenja) {
        this.merenja = merenja;

    }
}
