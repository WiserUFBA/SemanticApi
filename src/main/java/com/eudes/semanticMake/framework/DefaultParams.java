package com.eudes.semanticMake.framework;

import java.util.HashMap;

/**
 * Created by Eudes on 16/05/2018.
 */
public class  DefaultParams {
    HashMap<String, String> headers;
    DefaultParams (String attribute, String value){
        headers.put(attribute, value);
    }
}
