// Created by carme
// Creation date 01/12/2021

package com.example.thevault.service;

import com.example.thevault.domain.mapping.repository.RootRepository;
import com.example.thevault.domain.model.Asset;
import com.example.thevault.domain.model.Cryptomunt;
import com.example.thevault.domain.model.Klant;
import com.example.thevault.domain.transfer.AssetDto;
import com.example.thevault.support.BSNvalidator;
import com.example.thevault.support.exceptions.AgeTooLowException;
import com.example.thevault.support.exceptions.IncorrectBSNException;
import com.example.thevault.support.exceptions.PasswordNotSuitableException;
import com.example.thevault.support.exceptions.RegistrationFailedException;
import com.example.thevault.support.hashing.BCryptWachtwoordHash;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class KlantService {

    private final RootRepository rootRepository;
    private final Logger logger = LoggerFactory.getLogger(KlantService.class);
    public final static int VOLWASSEN_LEEFTIJD = 18;
    public final static int MINIMALE_WACHTWOORDLENGTE = 8; // TODO navragen of dit public of private moet

    @Autowired
    public KlantService(RootRepository rootRepository) {
        super();
        this.rootRepository = rootRepository;
        logger.info("New KlantService.");
    }

    public Klant vindKlantByGebruikersnaam(String username){
        return rootRepository.vindKlantByGebruikersnaam(username);
        //TODO de methode 'geefNuttigePortefeuille' aanroepen om de asset objecten voor de klant op te schonen (zie onder)
    }

    /**
     * @Author Carmen
     * In de portefeuille van de klant worden de assets vervangen door AssetDTO objecten, waarbij alleen de
     * voor de klant nuttige informatie wordt doorgegeven (deze methode stond eerst in AssetService)
     * @param klant de klant die de portefeuille oproept
     * @return List</AssetDto> een lijst met alle assets van de klant, zijnde de portefeuille, in de vorm die voor de
     * klant meerwaarde heeft
     */
    public List<AssetDto> geefNuttigePortefeuille(Klant klant){
        List<AssetDto> portefeuilleVoorKlant = new ArrayList<>();
        for (Asset asset : klant.getPortefeuille()) {
            portefeuilleVoorKlant.add(new AssetDto(asset, rootRepository.haalMeestRecenteCryptoWaarde(asset.getCryptomunt())));
        }
        return portefeuilleVoorKlant;
    }

    /**
     * Methode die zoekt naar een cryptomunt in de portefeuille van een klant
     * en de asset daarvan teruggeeft.
     *
     * @param klant van de portefeuille
     * @param cryptomunt waarnaar gezocht wordt
     *
     * @return asset null of met cryptomunt + aantal van klant
     */
    public Asset geefAssetMetCryptoMuntVanKlant(Klant klant, Cryptomunt cryptomunt){
        for (Asset asset: klant.getPortefeuille()) {
            if(asset.getCryptomunt()==cryptomunt){
                return asset;
            }
        }
        return null;
    }


    /**
     * Deze methode probeert een nieuwe klant te registreren.
     * Als de gegevens correct zijn ingevuld en de gebruikersnaam nog niet bestaat,
     * wordt het wachtwoord eerst gehasht en daarna gecodeerd.
     * Vervolgens wordt de klant opgeslagen in de database.
     *
     * @param klant een Klant-object is wordt aangemaakt op basis van ingevoerde gegevens
     * @return het klant-object met het gealtereerde wachtwoord
     */

    public Klant registreerKlant(Klant klant){
        bsnExceptionHandler(klant);
        wachtWoordExceptionHandler(klant);
        minumumLeeftijdExceptionHandler(klant);
        gebruikersnaamExceptionHandler(klant);
        String teHashenWachtwoord = klant.getWachtwoord();
        String gehashtWachtwoord = BCryptWachtwoordHash.hashWachtwoord(teHashenWachtwoord); // hash wachtwoord
        gehashtWachtwoord = Base64.encodeBase64String(gehashtWachtwoord.getBytes(StandardCharsets.UTF_8)); // versleutel gehasht wachtwoord
        klant.setWachtwoord(gehashtWachtwoord);
        rootRepository.slaKlantOp(klant);
        return klant;
    }

    /**
     * Deze methode zorgt ervoor dat bij een al bestaande gebruikersnaam de juiste exception wordt aangeroepen
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     */
    private void gebruikersnaamExceptionHandler(Klant klant) {
        if(vindKlantByGebruikersnaam(klant.getGebruikersnaam()) != null){
            throw new RegistrationFailedException();
        }
    }
    /**
     * Deze methode zorgt ervoor dat bij een te lage leeftijd de juiste exception wordt aangeroepen
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     */
    private void minumumLeeftijdExceptionHandler(Klant klant) {
        if(!checkVolwassen(klant)){
            String message = String.format("Je moet %d zijn om een rekening te openen.", VOLWASSEN_LEEFTIJD);
            throw new AgeTooLowException(message);
        }
    }

    /**
     * Deze methode zorgt ervoor dat wanneer het wachtwoord niet aan de eisen voldoet,
     * de juiste exception wordt aangeroepen
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     */
    private void wachtWoordExceptionHandler(Klant klant) {
        if(!checkWachtwoordLengte(klant) || !checkWachtwoordFormat(klant)){
            String message = String.format("Het wachtwoord moet minimaal %d karakters lang zijn" +
                    " en mag geen spaties bevatten", MINIMALE_WACHTWOORDLENGTE);
            throw new PasswordNotSuitableException(message);
        }
    }
    /**
     * Deze methode zorgt ervoor dat bij een niet correct ingevoerde BSN de juiste exception wordt aangeroepen
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     */
    private void bsnExceptionHandler(Klant klant) {
        if(!BSNvalidator.bsnValideren(klant.getBsn())){
            throw new IncorrectBSNException();
        }
    }

//    /**
//     * Wim 20211207
//     * @param gebruikersNaam
//     * @param wachtwoord
//     * @return Klant als combinatie gebruikersnaam en wachtwoord correct is, anders geef foutmelding
//     */
//    public Klant valideerLogin (String gebruikersNaam, String wachtwoord) throws LoginFailedException {
//        //vraag wachtwoord op via RootRepos
//        if(vindKlantByGebruikersnaam(gebruikersNaam) == null){
//            throw new LoginFailedException();
//        }
//       if(!BCryptWachtwoordHash.verifyHash(wachtwoord, vindKlantByGebruikersnaam(gebruikersNaam).getWachtwoord())){
//           throw new LoginFailedException();
//       }
//        return vindKlantByGebruikersnaam(gebruikersNaam);
//    }

    /**
     * Deze methode kijkt of de ingevulde geboortedatum van de klant
     * minimaal 18 jaar in het verleden ligt.
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     * @return een boolean die aangeeft of de klant volwassen is
     */
    public boolean checkVolwassen(Klant klant){ // 1 dag correctie nodig voor groter / gelijk
         return LocalDate.now().minusYears(VOLWASSEN_LEEFTIJD).plusDays(1).isAfter(klant.getGeboortedatum());
    }

    /**
     * Deze methode gaat na of het wachtwoord aan de minimumeis qua lengte voldoet
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     * @return een boolean die aangeeft of het wachtwoord lang genoeg is
     */
    public boolean checkWachtwoordLengte(Klant klant){
        return klant.getWachtwoord().length() >= MINIMALE_WACHTWOORDLENGTE;
    }

    /**
     * Deze methode gaat na of het wachtwoord geen spaties bevat
     *
     * @param klant de ingevoerde klantgegevens in objectvorm
     * @return een boolean die aangeeft of het wachtwoord geen spaties bevat
     */
    public boolean checkWachtwoordFormat(Klant klant){
       return !klant.getWachtwoord().contains(" ");
    }

    public RootRepository getRootRepository() {
        return rootRepository;
    }
}
