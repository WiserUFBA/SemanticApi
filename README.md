
# SemanticMake

Project developed to assist the development of web applications that aim to store semantic content. This framework was
developed in Java a with the help of the JENA framework to get more facilitates in the the manipulation of 
semantic content. 

## Content
- [Requisites](#requisites)
- [Use](#use)
- [ClientApi](#client-api) 
    - [Property Format](#resource-format)
    - [Sample of use](#sample-of-use)
    - [Swagger UI (Application tests)](#swagger-ui)
- [Documentation](#documentation)
    - [Javadoc](#javadoc)

### Requisites
  
- [Java 8+](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
- [Fuseki 3+](https://jena.apache.org/download/#jena-fuseki): Must be running and have a dataset called SemanticContent.
- [Intellij Lombok Plugin](https://github.com/mplushnikov/lombok-intellij-plugin): Version: 0.14.16 
- [Spring Framework](https://projects.spring.io/spring-boot/): Boot starter version: 1.5.7
- [Maven 3+](https://maven.apache.org/install.html): This dependency manager is responsible for all dependencies of this project 

### Use

It's necessary import client library into JavaScript that is directed to this API. There are methods to add
vocabularies (addVocabulary), triples (addTriple) and save the contents of the resource (saveResource). There are four
representation of content:

- A property like triple that guards subject, predicate and literal
    ``` javascript
        <foaf:familyName>Brickley</foaf:familyName>
        [{
            "propertyName":"familyName",
            "value":"Brickley",
            "asResouce":false,
            "subPropertyOf":"",
            "prefix":"foaf"
        }]
    ``` 
- A property like triple that holds subject, predicate and URI of another resource
    ``` javascript
        <foaf:openid rdf:resource="http://danbri.org/" />
        [{
            "propertyName":"openid",
            "value":"http://danbri.org/",
            "asResouce":true,
            "subPropertyOf":"",
            "prefix":"foaf"
        }]    
    ```
- A subpropriety as triple that guards subject, predicate and literal
     ``` javascript
        <vcard:hasAddred>
            <vcard:country-name>Brazil</vcard:country-name>
        </vcard:hasAddred>
        [
            {
                "propertyName":"hasAddress",
                "value":"",
                "asResouce":true,
                "subPropertyOf":"",
                "prefix":"vcard"
            },
            {
                "propertyName":"country-name",
                "value":"Brazil",
                "asResouce":false,
                "subPropertyOf":"hasAddress",
                "prefix":"vcard"
            }
        ]        
     ```
- A subpropriety as a triple that holds subject, predicate and URI of another resource
     ``` javascript
        <vacard:hasEmail>
            <vcard:hasValue rdf:resource="eudesdionatas@gmail.com" />
        </vacard:hasEmail>
        [
            {
                "propertyName":"hasEmail",
                "value":"",
                "asResouce":true,
                "subPropertyOf":"",
                "prefix":"vcard"
            },
            {
                "propertyName":"hasValue",
                "value":"eudesdionatas@gmail.com",
                "asResouce":true,
                "subPropertyOf":"hasEmail",
                "prefix":"vcard"
            }
        ]
     ```

To clarify how the data should be passed, an interface has been developed that shows the structure of the data
as they are formed and in parallel are presented the arguments that must be passed to form the structure
in question.

### Client API

There is a constructor method, "SemanticAPI", which receives the base url that has the address of where the api is. 
To create a new resource you must use the constructor method of the "Resource" class. To add new vocabularies to a 
resource you must use the "addVocabulary" method of Resource, to add new triples you must use the "addTriple" method of 
"Resource". At the end of this document there is a section showing an example of using the client side for API. Such methods 
are useful for facilitating the creation of the resource in an atomic operation.

### Resource Format

```javascript
{
    "prefix": "resourcePrefix",
    "name": "resourceName",
    "uri": "resourceURI",
    "vocabularies": [
        { "uri": "vocabURI",
          "prefix": "vocabPrefix",
          "pairs": [
            { "propertyName": "propertyName",
              "value": "propertyValue"
              "asResource": true
              "subPropetyOf": "anotherProperty"  
            }
          ]
        }
    ] 
}
```

## Sample of use

```javascript
   const config = { 
     baseURL: 'http://localhost:8080/'
     workspace: 'workspace-123456'
   }
   const api = new SemanticAPI(config)
   const resource = new Resource('contactData', 'contact', 'http://contactmail.com#Person')
   resource.addVocabulary('schema', 'http://schema.org/')
   resource.addTriple('schema', 'email', 'emailValue')
   resource.addTriple('schema', 'name', 'nameValue')
   const resourceToSend = resource.getResourceToSend()
   api.saveResource(resourceToSend)
     .then(...)
     .catch(...)
```

## Documentation

In the next two sections is showed two ways to present the operation of the API

## Javadoc

The Javadoc folder contains all the documentation of all Java files that were used.

## Swagger UI

To test the operation of all API methods, [Swagger](http://localhost:8080/swagger-ui.html) provides an interface with 
input field, use and output examples to test the tool. This was the alternative used for documentation of REST methods.
To open the Swagger the application SemanticApi need be running.

## Support and development

<p align="center">
Developed by Eudes Souza with Intellij IDEA at </br>
<img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>
