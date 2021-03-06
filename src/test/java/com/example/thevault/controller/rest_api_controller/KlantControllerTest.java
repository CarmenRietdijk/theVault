package com.example.thevault.controller.rest_api_controller;

import com.example.thevault.domain.mapping.repository.RootRepository;
import com.example.thevault.domain.model.Adres;
import com.example.thevault.domain.model.Klant;
import com.example.thevault.domain.model.Rekening;
import com.example.thevault.domain.transfer.LoginDto;
import com.example.thevault.service.LoginService;
import com.example.thevault.domain.transfer.RegistrationDto;
import com.example.thevault.service.RegistrationService;
import com.example.thevault.service.KlantService;
import com.example.thevault.support.BSNvalidator;
import com.example.thevault.support.authorization.AuthorizationService;
import com.example.thevault.support.authorization.TokenKlantCombinatie;
import com.example.thevault.support.hashing.BCryptWachtwoordHash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.security.auth.login.LoginException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

@WebMvcTest
class KlantControllerTest {

    private MockMvc mockMvc;

    private KlantController klantController;
    @MockBean
    private KlantService klantService;
    @MockBean
    private RegistrationService registrationService;
    @MockBean
    private LoginService loginService;
    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    public KlantControllerTest(MockMvc mockMvc){
        super();
        this.mockMvc = mockMvc;
    }

    String gehashtWachtwoord;
    Adres adres;
    Klant testKlant;
    Rekening rekening;
    String gebruikersnaam;
    String wachtwoord;
    LoginDto loginDto2;


    @BeforeEach
    void setUp() {
        gehashtWachtwoord = BCryptWachtwoordHash.hashWachtwoord("testWW");
        adres = new Adres("straat", 357, "C", "1000AA", "Amsterdam");
        testKlant = new Klant("testKlant", gehashtWachtwoord,null, null, "Jan",
                adres, BSNvalidator.TESTBSN_VAN_RIVG, LocalDate.of(1975, 7, 30));
        testKlant.setGebruikerId(2);
        rekening = new Rekening("NL20INGB0006582287", 1000);
        gebruikersnaam = "testKlant";
        wachtwoord = "testWW";
        loginDto2 = new LoginDto("testKlant", "ww2");

//        loginDto = new LoginDto("testKlant", "testWW");

    }


    @Test
    void registreerKlantHandler() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String testKlantJson = objectMapper.writeValueAsString(testKlant);
        testKlant.setRekening(rekening);
        RegistrationDto testRegistrationDto = new RegistrationDto(testKlant);
        Mockito.when(registrationService.registreerKlant(testKlant)).thenReturn(testRegistrationDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/register");
        requestBuilder.content(testKlantJson).contentType(MediaType.APPLICATION_JSON);
        try {
            ResultActions response = mockMvc.perform(requestBuilder);
            response.andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(MockMvcResultMatchers
                            .content()
                            .json("{\"naam\":\"Jan\"," +
                            "\"gebruikersnaam\":\"testKlant\",\"iban\":\"NL20INGB0006582287\"," +
                                    "\"postcodeEnHuisnummer\":\"1000AA / 357C\"}"))
                    .andDo(MockMvcResultHandlers.print());
        } catch (Exception e){
            System.out.println("nee fout: " + e);
            fail();
        }
    }

    @Test
    void loginHandler() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        //loginDto wordt door noArgs constr opgepakt, waarom?


        String testInlogJson = objectMapper.writeValueAsString(loginDto2);
        Klant klant = new Klant("testKlant", gehashtWachtwoord,null, null, "Jan",
                adres, BSNvalidator.TESTBSN_VAN_RIVG, LocalDate.of(1975, 7, 30));
        testKlant.setRekening(rekening);


        Mockito.when(loginService.valideerLogin(loginDto2)).thenReturn(testKlant);
        Mockito.when(authorizationService.authoriseerKlantMetRefreshToken(testKlant)).thenReturn(new TokenKlantCombinatie(UUID.randomUUID(), testKlant));
        Mockito.when(authorizationService.genereerAccessToken(testKlant)).thenReturn("testTransparentToken");
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/login");
        requestBuilder.content(testInlogJson).contentType(MediaType.APPLICATION_JSON);

        try {
            ResultActions response = mockMvc.perform(requestBuilder);
            response.andExpect(MockMvcResultMatchers.status().isOk())
                    /*                   .andExpect(MockMvcResultMatchers
                                   .content()
                                       .json("{\"naam\":\"Jan\"," +
                                       "\"gebruikersnaam\":\"testKlant\",\"iban\":\"NL20INGB0006582287\"," +
                                       "\"postcodeEnHuisnummer\":\"1000AA / 357C\"}"))*/
                    .andDo(MockMvcResultHandlers.print());

        }catch (Exception e){
            System.out.println("dit gaat niet goed: " + e);
            fail();
        }

    }
}