package com.example.travis.ichmfapp.main;

import com.rapidapi.rapidconnect.*;
import java.util.*;

/**
 * Created by Travis on 22/10/2018.
 */

public class NewtonAPI {

    private String expression;
    private static RapidApiConnect connect;

    public NewtonAPI(){
        connect = new RapidApiConnect("ichmf_5bcc9fc6e4b0d1763ed680f2", "76d1c3f6-4ae6-4f21-bef5-1ae52916d378");
    }

    public void getAnswer(){

        Map<String, Argument> body = new HashMap<String, Argument>();

        body.put("expression", new Argument("data", "x^2+x"));

        try {
            Map<String, Object> response = connect.call("Newton", "factoringExpression", body);
            if(response.get("success") != null) {

            } else{


            }
        } catch(Exception e){


        }
    }







}





