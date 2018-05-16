package com.eudes.semanticMake.util;

import com.eudes.semanticMake.api.Pair;
import lombok.Data;

/**
 * Class to assist in structuring the properties to be inserted into a resource
 */
@Data
public class Verified {

    /**
     * The contructor the receive one Pair to make the object to be verfied
     * @param data The Pair
     */
    public Verified(Pair data){
        this.pair = data;
        this.verified = false;
    }

    /**
     * Type of object to verify, in this case a Pair that represents a property
     */
    private Pair pair;

    /**
     * Flag to verify if the data was verified
     */
    private boolean verified = false;

    /**
     * Method that return the state of object
     * @return The state of object
     */
    public boolean isVerified(){
        return this.verified;
    }




}
