package com.example.thevault.domain.mapping.dao;

import com.example.thevault.domain.model.Asset;
import com.example.thevault.domain.model.Cryptomunt;
import com.example.thevault.domain.model.Klant;

/**
 * @Author: Carmen Rietdijk
 * Beschrijving: De interface voor de DAO voor Cryptomunt, waar methodes in komen voor CRUD van de Cryptomunten
 * Op dit moment alleen 'getCryptomunt' ten behoeve van het gebruik van Assets in de RootRepo
 */

public interface CryptomuntDAO {

    /**
     * Dit betreft het vinden van een specifieke cryptomunt
     * @param cryptomuntId cryptomuntidentifier waarover informatie wordt opgevraagd
     * @return Cryptomunt de cryptomunt waarover informatie is opgevraagd
     */
    public Cryptomunt geefCryptomunt(int cryptomuntId);
}
