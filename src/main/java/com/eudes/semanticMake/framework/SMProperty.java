package com.eudes.semanticMake.framework;

/**
 * Created by Eudes on 16/05/2018.
 */
public class SMProperty {
    /**
     * Predicate name
     */
    private String propertyName;

    /**
     * Predicate value
     */
    private String value;

    /**
     * Flag to verify the representation of the resource
     */
    private boolean asResource;

    /**
     * String that contains the name of the property to which this property belongs
     */
    private String subPropertyOf;


    public SMProperty(String propertyName, String value, boolean asResource, String subPropertyOf) {
        this.propertyName = propertyName;
        this.value = value;
        this.asResource = asResource;
        this.subPropertyOf = subPropertyOf;
    }

    public String getPropertyName() {

        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean asResource() {
        return asResource;
    }

    public void setAsResource(boolean asResource) {
        this.asResource = asResource;
    }

    public String getSubPropertyOf() {
        return subPropertyOf;
    }

    public void setSubPropertyOf(String subPropertyOf) {
        this.subPropertyOf = subPropertyOf;
    }
}
