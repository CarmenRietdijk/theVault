package com.example.thevault.domain.mapping.dao;

import com.example.thevault.domain.model.Adres;
import com.example.thevault.domain.model.Klant;

public interface AdresDAO {
    public Adres slaAdresOp(Adres adres);

    public Adres wijzigAdres(Adres adres);

    public Adres getAdresById(int adresId);

    public Adres getAdresByKlant(Klant klant);
}
