// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalwebsample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller exposing application endpoints
 */
@RestController
@RequestMapping("/")
public class AuthPageController {

    @Autowired
    AuthHelper authHelper;

    //@RequestMapping("/msal4jsample")
    public ModelAndView  homepage(HttpServletRequest httpRequest, HttpServletResponse response){


        if(httpRequest.isRequestedSessionIdValid()) {
            //Si quitas de la carpeta templates el index en automatico se ira el redirect al index
            // de la carpetita static, pero puedes poner practiamente cuanlquier pagina en el redirect

            ModelAndView mav = new ModelAndView("redirect:/index.html");
            return mav;
        }else{
            ModelAndView mav = new ModelAndView("/indexooo.html");
            return mav;
        }

    }
    //Vamos a redireccionar

    @RequestMapping("/")
    public ModelAndView  homepage2(HttpServletRequest httpRequest, HttpServletResponse response){


        if(httpRequest.isRequestedSessionIdValid()) {
            //Si quitas de la carpeta templates el index en automatico se ira el redirect al index
            // de la carpetita static, pero puedes poner practiamente cuanlquier pagina en el redirect

            ModelAndView mav = new ModelAndView("redirect:/index.html");
            return mav;
        }else{
            ModelAndView mav = new ModelAndView("/indexooo.html");
            return mav;
        }

    }
    @RequestMapping("/msal4jsample/secure/aad/dos")
    public ModelAndView securePage(HttpServletRequest httpRequest) throws ParseException {
        ModelAndView mav = new ModelAndView("/dos/index");

      // setAccountInfo(mav, httpRequest);

        return mav;
    }
//ESTE ES EL QUE ME AUTODIRIGE A LA VERGA
    @RequestMapping("/msal4jsample/secure/aad")
    public ModelAndView securePage2(HttpServletRequest httpRequest) throws ParseException {
     //   ModelAndView mav = new ModelAndView("auth_page");
        ModelAndView mav = new ModelAndView("redirect:/index.html");

        setAccountInfo(mav, httpRequest);



        return mav;
    }

    @RequestMapping("/msal4jsample/secure/aad/tres")
    public String secureTres() throws ParseException {
        ModelAndView mav = new ModelAndView("auth_page");





         return"Ya me canse sumamente molesto";
    }


    @RequestMapping("/msal4jsample/sign_out")
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {

        httpRequest.getSession().invalidate();


        String endSessionEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/logout";

        String redirectUrl = "https://daton.herokuapp.com/";
        response.sendRedirect(endSessionEndpoint + "?post_logout_redirect_uri=" +
                URLEncoder.encode(redirectUrl, "UTF-8"));
    }

    @RequestMapping("/msal4jsample/graph/me")
    public ModelAndView getUserFromGraph(HttpServletRequest httpRequest, HttpServletResponse response)
            throws Throwable {

        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelper.getAuthResultBySilentFlow(httpRequest, response);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);

                String authorizationCodeUrl = authHelper.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelper.getRedirectUriGraph(),
                        state,
                        nonce);

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page");
            setAccountInfo(mav, httpRequest);

            try {
                mav.addObject("userInfo", getUserInfoFromGraph(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    private String getUserInfoFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        URL url = new URL("https://graph.microsoft.com/v1.0/me");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return responseObject.toString();
    }

    private void setAccountInfo(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = SessionManagementHelper.getAuthSessionObject(httpRequest);

        String tenantId = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getStringClaim("tid");
      System.out.println("EXPIRAAAAAAAAAAAAAA "+ auth.expiresOnDate().toString());

        model.addObject("tenantId", tenantId);
        model.addObject("account", SessionManagementHelper.getAuthSessionObject(httpRequest).account());
    }
}
