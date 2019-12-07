package com.microsoft.azure.msalwebsample;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@RestController
@RequestMapping("/")

public class ControladorHola {
    @GetMapping("/msal4jsample/secure/aad/cuatro")
    public String hola(){
        return "Hola mundo cuatro";
    }
    @GetMapping("//msal4jsample/secure/aad")
    public String hola2() throws UnknownHostException {
        //   String ip = InetAddress.getLocalHost().getHostAddress();ssss


        return "Se guardo que pasa ";
    }


}
