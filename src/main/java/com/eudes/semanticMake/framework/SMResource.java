package com.eudes.semanticMake.framework;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eudes on 15/05/2018.
 */
public class SMResource {

    private HashMap<String, SMVocabulary> vocabularies;
    private String type;
    private String prefix;
    private String uri;

    SMResource(String name, String prefix, String uri, HashMap<String, SMVocabulary> vocabularies){
        this.type           = name;
        this.prefix         = prefix;
        this.uri            = uri;
        this.vocabularies   = vocabularies;
    }

    public HashMap<String, SMVocabulary> getVocabularies() { return vocabularies; }

    public void setVocabularies(HashMap<String, SMVocabulary> vocabularies){
        this.vocabularies = vocabularies;
    }

    public String getType() { return type; }

    public void setType(String type){
        this.type = type;
    }

    public String getPrefix() { return prefix; }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public String getUri() { return uri; }

    public void setUri(String uri) { this.uri = uri; }

    public void setAbout(String uri){
        this.uri = uri;
    }

    public String getAbout (){ return this.uri;}

    public void addVocabulary(String vocabPrefix, String uri){
      SMVocabulary vocab = new SMVocabulary(vocabPrefix, uri);
        if(!this.vocabExists(vocabPrefix, this.vocabularies))
            this.vocabularies.put(vocabPrefix, vocab);
    }

    public boolean vocabExists(String vocabPrefix, HashMap<String, SMVocabulary> vocabularies){
        if(vocabularies.get(vocabPrefix) == null)
            return false;
        return true;
    }

    public void addProperty(String vocabPrefix, SMProperty property){
        String  propName        = property.getPropertyName();
        String  value           = property.getValue();
        boolean asResource      = property.asResource();
        String subPropertyOf    = property.getSubPropertyOf();

        if (subPropertyOf.equals("") || subPropertyOf == null){
            SMProperty auxProperty = new SMProperty(propName, value, asResource, "");
            this.vocabularies.get(vocabPrefix).getProperties().add(auxProperty);
        } else {
            if (!this.propertyExists(vocabPrefix, subPropertyOf)){
        SMProperty auxProperty = new SMProperty(subPropertyOf, "", true, "");
                this.vocabularies.get(vocabPrefix).getProperties().add(auxProperty);
            }
      SMProperty subproperty = new SMProperty (propName, value, asResource, subPropertyOf);
            this.vocabularies.get(vocabPrefix).getProperties().add(subproperty);
        }
    }

    public boolean propertyExists(String vocabPrefix, String subPropertyOf){
        for (SMProperty property: this.vocabularies.get(vocabPrefix).getProperties()){
            if(property.getPropertyName() == subPropertyOf)
                return true;
        }
        return false;
    }

    public SMResource getResourceToSend(){
        return  this;
    }

    public void addLanguage(String language) {
        if(!this.vocabularyDctermsExists()){
        SMVocabulary vocab = new SMVocabulary("dcterms", "http://purl.org/dc/terms/");
            this.vocabularies.put("dcterms", vocab);
        }
        SMProperty lang = new SMProperty("language", language, false, "");
        this.vocabularies.get("dcterms").getProperties().add(lang);
    }

    public boolean vocabularyDctermsExists(){
        for (SMVocabulary vocabulary: this.vocabularies.values())
            if (vocabulary.getPrefix().equals("dcterms"))
                return true;
        return false;
    }


    public void addDateTime(){
        if(!this.vocabularyIcalExists()){
        SMVocabulary vocab = new SMVocabulary("ical", "http://www.w3.org/2002/12/cal/ical#");
            this.vocabularies.put("ical", vocab);
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date dateTimeCreated = new Date();
        SMProperty created = new SMProperty( "created", dateFormat.format(dateTimeCreated), false, "");
        this.vocabularies.get("ical").getProperties().add(created);

    }

    public boolean vocabularyIcalExists(){
        for (SMVocabulary vocabulary: this.vocabularies.values())
        if (vocabulary.getPrefix() == "ical")
            return true;
        return false;
    }

}

