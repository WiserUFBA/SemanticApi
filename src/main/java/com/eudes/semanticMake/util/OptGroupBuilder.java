package com.eudes.semanticMake.util;


import com.eudes.semanticMake.api.OptGroup;
import com.eudes.semanticMake.api.Option;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RIOT;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for reading the vocabulary data
 * @author Eudes Souza
 * @since 10/2017
 */
public class OptGroupBuilder {

    /**
     * Method responsible for constructing a Map containing OptGroup's where each OptGroup maintains the title (text) of the group of properties and a list of properties (children)
     * @param vocabUri String with the URI of the vocabulary
     * @param vocabFilePath String with the vocabulary path to be read
     * @param search String with search term
     * @return Map of OptGroup's vocabulary properties
     */
    public Map<String, OptGroup> build(String vocabUri, String vocabFilePath, String search) {
        RIOT.init();
        InputStream vocabInputStream = getClass().getResourceAsStream(vocabFilePath);
        Model model = ModelFactory.createDefaultModel();
        String lang = vocabFilePath.endsWith("rdf") ? "RDF/XML" : "TURTLE";
        model.read(vocabInputStream, null, lang);

        Map<String, OptGroup> optGroupMap = new HashMap<>();

        model.listSubjects().forEachRemaining(res -> {
            Statement typeStmt = res.getProperty(RDF.type);
            if (typeStmt == null) return;

            Resource resource = typeStmt.getResource();
            String typeResourceUri = resource.getURI();
//            System.out.println(typeResourceUri);
            if (!isPropertyResource(typeResourceUri)) return;

            String property = res.getURI().replace(vocabUri, "");
            if (search != null && !property.toLowerCase().contains(search.toLowerCase())) return;

            String domain = "Without specific domain";
            Statement domainStmt = res.getProperty(RDFS.domain);
            if (domainStmt != null) {
                Resource domainResource = domainStmt.getResource();
                if (domainResource != null) {
                    String domainUri = domainResource.getURI();
                    domain = domainUri != null ? domainUri.replace(vocabUri, "") : domain;
                }
            }

            if (domain.startsWith("http")) return;

            optGroupMap.putIfAbsent(domain, new OptGroup(domain, new ArrayList<>()));
            OptGroup optGroup = optGroupMap.get(domain);
            optGroup.getChildren().add(new Option(property, property));
        });

        return optGroupMap;
    }

    /**
     * Method responsible for building the list of properties without considering any search term
     * @param vocabUri String containing the URI of the vocabulary to be read
     * @param vocabFilePath String containing the vocabulary path to be read
     * @return Map of OptGroup's vocabulary properties
     */
    public Map<String, OptGroup> build(String vocabUri, String vocabFilePath) {
        return  build(vocabUri, vocabFilePath, null);
    }

    /**
     * Method responsible for analyzing whether the data (resource) read in the vocabulary is a property
     * @param uri String containing the URI of the vocabulary resource to be parsed
     * @return Returns a boolean with a value of true when the URI equals one of the RDF.Property constants, OWL.DatatypeProperty
     * or OWL.ObjectProperty. It is returned false otherwise
     */
    private boolean isPropertyResource(String uri) {
        return RDF.Property.getURI().equals(uri)
                || OWL.DatatypeProperty.getURI().equals(uri)
                || OWL.ObjectProperty.getURI().equals(uri);
    }

}
