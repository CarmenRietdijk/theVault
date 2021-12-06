package com.example.thevault.domain.mapping.dao;

import com.example.thevault.domain.model.Klant;
import com.example.thevault.domain.model.Rekening;


public interface RekeningDAO {

    void slaRekeningOp(Rekening rekening);

    Rekening vindRekeningVanKlant(Klant klant);

    double vraagSaldoOpVanKlant(Klant klant);

    void wijzigSaldoVanKlant(Klant klant, double bedrag);


}