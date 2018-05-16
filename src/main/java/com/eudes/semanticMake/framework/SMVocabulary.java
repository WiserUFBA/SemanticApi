package com.eudes.semanticMake.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eudes on 16/05/2018.
 */
public class SMVocabulary {

        private List<SMProperty> properties = null;
        private String prefix               = null;
        private String uri                  = null;

        SMVocabulary(String prefix,String uri){
            this.properties = new ArrayList<SMProperty>();
            this.prefix     = prefix;
            this.uri        = uri;
        }

    public List<SMProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<SMProperty> properties) {
        this.properties = properties;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
