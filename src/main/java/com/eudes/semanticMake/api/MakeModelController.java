package com.eudes.semanticMake.api;

import com.eudes.semanticMake.util.KnownVocabs;
import com.eudes.semanticMake.util.OptGroupBuilder;
import com.eudes.semanticMake.util.Verified;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.util.StringUtils.hasText;

/**
 * REST controller responsible for handling requests and responses to the client
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@RestController
@RequestMapping(value = "/resources")
@Api(value="onlinestore", description="REST controller responsible for handling requests and responses to the client")
public class MakeModelController {

    /**
     * String that stores the URL of the Fuseki/DatasetName server to store the ontologies
     */
    private String fusekiURI = "http://localhost:3030/SemanticContent";

    /**
     *String that hold the name to the graph of Dataset
     */
    private String graphURI;

    /**
     * DatasetAccessor that hold the URI of the server and the graph name that will store the resources
     */
    private DatasetAccessor datasetAccessor = null;

    /**
     * Start MakeModelController with the URL of the Fuseki server where the ontologies will be stored
     */
    public MakeModelController() {
        datasetAccessor = DatasetAccessorFactory.createHTTP(fusekiURI);
    }


    /**
     * Method responsible for assign the triple store address e the dataset name
     * @param datasetAddress String to hold the triple store address
     * @param datasetName String to hold the dataset name
     * @return Returns true
     */
    @PostMapping("/setTripleStoreAddress")
    @ApiOperation(value = "Method for assign the triple store address", response = ResponseEntity.class)
    public ResponseEntity setTripleStoreAddres(@RequestParam String datasetAddress, @RequestParam String datasetName){
        fusekiURI = datasetAddress + "/" + datasetName;
        System.out.print("Mudou o endereço da triple store para " + fusekiURI);
        return ResponseEntity.ok(true);
    }

    /**
     * Method responsible for receiving and saving the resource passed in the client defined ontology
     * <br> The resource is passed in JSON format
     * @param resource Resource passed by client
     * @param workspace Returns a ResponseEntity containing a APIResponse
     * @return Returns a ResponseEntity containing the saved APIResponse
     */
    @PostMapping("/saveResource/{workspace}")
    @ApiOperation(value = "Method responsible to save a new resource", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Saves the resource successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    public ResponseEntity saveResource(@RequestBody ResourceApi resource, @PathVariable String workspace) {
        resource.setAbout(normalizeURI(resource.getAbout()));
        Model model = createModel(resource);
        addAsResource(model, resource);
        model.write(System.out);
        String resourceId  = "";

        StmtIterator iter = model.listStatements();
        while(iter.hasNext() && !resourceId.contains("http://")){
            resourceId  = iter.nextStatement().getSubject().toString();
        }

        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
        try{
            if(datasetAccessor.getModel(graphURI) == null)
                graphURI = "/workspace-" + UUID.randomUUID().toString().substring(0,8);
        }catch (Exception e){
            System.out.println("Não conseguiu recuperar o modelo com o workspace passado");
        }

        datasetAccessor.add(graphURI, model);
        return ResponseEntity.ok(new APIResponse(graphURI, resourceId, null));
    }

    /**
     * Method responisble for delete one especified resouce
     * @param workspace String to hold the name of the workspace where the resource is
     * @param resourceId String to hold the ID of the resource to be deleted
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @DeleteMapping("/deleteResource/{workspace}")
    @ApiOperation(value = "Method responsible to delete a resource given the resource uri", response = ResponseEntity.class)
    public ResponseEntity deleteResource(@PathVariable String workspace, @RequestParam String resourceId) {

        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
        Model model = datasetAccessor.getModel(graphURI);
        Resource r = model.getResource(resourceId);
        StmtIterator iter = r.listProperties();
        List<Statement> innerResources = new ArrayList<>();
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();
            RDFNode object      = stmt.getObject();
            if (object instanceof Resource && !object.toString().contains("http://")) {
                innerResources.add(stmt);
            }
        }
        for (Statement innerResource : innerResources){
            ((Resource)innerResource.getObject()).removeProperties();
            model.remove(r, innerResource.getPredicate(), innerResource.getObject());
        }

        r.removeProperties();
        datasetAccessor.putModel(graphURI, model);
        return ResponseEntity.ok(new APIResponse(graphURI, resourceId, null));
    }

    /**
     * Method responsible for to delete one specifc graph given his name that we call workspace
     * @param workspace String to hold the name of the workspace to be deleted
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @DeleteMapping("/deleteGraph/{workspace}")
    @ApiOperation(value = "Method responsible to delete a especifc graph given the graph name", response = ResponseEntity.class)
    public ResponseEntity deleteGraph (@PathVariable String workspace){

        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
//        Model model = datasetAccessor.getModel(graphURI);
//        model.getGraph().clear();
        datasetAccessor.deleteModel(graphURI);
        return ResponseEntity.ok(new APIResponse(graphURI, null, null));
    }

    /**
     * Method responsible for to delete one specifc property in a resource
     * @param workspace String to hold the graph name that contains the resource
     * @param resourceId String to hold the resource URI
     * @param propertyUri String to hold the property URI that will be deleted
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @DeleteMapping("/deleteProperty/{workspace}")
    @ApiOperation(value = "Method responsible to delete a especific property of a resource given the property uri", response = ResponseEntity.class)
    public  ResponseEntity deleteProperty(@PathVariable String workspace, @RequestParam String resourceId, @RequestParam String propertyUri){

        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
        Model model = datasetAccessor.getModel(graphURI);
        Resource subject = model.getResource(resourceId);
        String propertyURI = getPropertyNameSpace(propertyUri);
        String propertyName = getPropertyName(propertyUri);
        Property prop = ResourceFactory.createProperty(propertyURI, propertyName);
        Statement stmt = subject.getProperty(prop);

        //If the resource have the property with a masterproperty
        if (stmt != null){
            StmtIterator propIter   = subject.listProperties();
            while(propIter.hasNext()){
                Statement propStmt  = propIter.nextStatement();
                Property masterProp = propStmt.getPredicate();
                if(masterProp.toString().equals(propertyUri)) {
                    prop            = masterProp;
                    RDFNode object  = stmt.getObject();
                    if(object instanceof Resource){
                        ((Resource) object).removeProperties();
                    }
                    break;
                }
            }
        }
        //If the resource don't have the property like a master property but can have with a subproperty
        else {
            StmtIterator propIter = subject.listProperties();
            while(propIter.hasNext()){
                Statement propStmt  = propIter.nextStatement();
                Property property   = propStmt.getPredicate();
                RDFNode object      = null;
                try{
                    object          = propStmt.getObject();
                }catch (Exception e){  }
                if (object != null && object instanceof Resource && property.toString().contains(propertyURI) && !object.toString().contains("http://") ) {
                    StmtIterator subIter    = ((Resource) object).listProperties();
                    subject                 = (Resource) object;
                    while(subIter.hasNext()){
                        Statement subStmt   = subIter.nextStatement();
                        Property subProp    = subStmt.getPredicate();
                        if(subProp.toString().equals(propertyUri)) {
                            prop            = subProp;
                        }
                    }
                }
            }
        }
        subject.removeAll(prop);
        datasetAccessor.putModel(graphURI, model);
        return ResponseEntity.ok( new APIResponse(graphURI, resourceId, propertyUri));
    }

    /**
     * Method responsible for to update one specifc property in a resource
     * @param workspace String to hold the graph name that contains the resource
     * @param resourceId String to hold the resource URI
     * @param propertyUri String to hold the property that will be deleted
     * @param newValue String to hold the new value to assign to the property
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @PutMapping("/updateProperty/{workspace}")
    @ApiOperation(value = "Method responsible to update a property of a resource", response = ResponseEntity.class)
    public ResponseEntity updateProperty(@PathVariable String workspace, @RequestParam String resourceId, @RequestParam String propertyUri, @RequestParam String newValue){
        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
        Model model = datasetAccessor.getModel(graphURI);
        Resource subject        = model.getResource(resourceId);
        String propertyURI      = getPropertyNameSpace(propertyUri);
        String propertyName     = getPropertyName(propertyUri);
        Property prop           = ResourceFactory.createProperty(propertyURI, propertyName);
        Statement stmt          = subject.getProperty(prop);
        boolean propAsResource  = false;
        boolean propExists      = false;
        //If the resource have the property with a masterproperty
        if (stmt != null){
            propAsResource = stmt.getObject().toString().contains("http://");

            StmtIterator propIter = subject.listProperties();
            while(propIter.hasNext()){
                Statement propStmt = propIter.nextStatement();
                Property masterProp = propStmt.getPredicate();
                if(masterProp.toString().equals(propertyUri)) {
                    prop        = masterProp;
                    propExists  = true;
                    break;
                }
            }
        }
        //If the resource don't have the property like a master property but can have with a subproperty
        else{
            StmtIterator propIter = subject.listProperties();

            while(propIter.hasNext()){
                Statement propStmt  = propIter.nextStatement();
                Property property   = propStmt.getPredicate();
                Resource object     = null;

                try{
                     object         = (Resource) propStmt.getObject();
                }catch (Exception e){  }

                if (object != null && object instanceof Resource && property.toString().contains(propertyURI) && !object.toString().contains("http://") ) {
                    StmtIterator subIter    = ((Resource) object).listProperties();
                    subject                 = (Resource) object;
                    while(subIter.hasNext()){
                        Statement subStmt   = subIter.nextStatement();
                        Property subProp    = subStmt.getPredicate();
                        RDFNode subObject   = subStmt.getObject();
                        if(subProp.toString().equals(propertyUri)) {
                            propAsResource  = subObject.toString().contains("http://");
                            prop            = subProp;
                            propExists      = true;
                        }
                    }
                }
            }
        }

        subject.removeAll(prop);

        if(propExists){
            if(propAsResource){
                Resource propResource = ResourceFactory.createResource(newValue);
                model.add(subject, prop, propResource);
            } else{
                subject.addProperty(prop,newValue);
            }
            datasetAccessor.putModel(graphURI, model);
            return ResponseEntity.ok(   new APIResponse(graphURI, resourceId, propertyURI)   );
        }
        else
            return ResponseEntity.ok(   new APIResponse(graphURI, resourceId, propertyURI)   );
    }

    /**
     * Method that retorns a list of resources given one URI of hid property and his respective value
     * @param workspace String to hold the graph name that will be passed
     * @param propertyUri String to hold the property URI
     * @param value String to hold the property value
     * @param isExactly Boolean used to determine if the passed value must be considered exactly like was passed
     *                  or if must only to contain the value passed on the property
     * @return Returns a ResponseEntity containing a list of resources
     */
    @GetMapping("/getResources/{workspace}")
    @ApiOperation(value = "Method responsible to get all resources that have one value of a property", response = ResponseEntity.class)
    public ResponseEntity<List<ResourceApi>> getResources(@PathVariable String workspace, @RequestParam String propertyUri, @RequestParam String value, @RequestParam boolean isExactly){
        graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
        Model model = datasetAccessor.getModel(graphURI);
        String propertyURI = getPropertyNameSpace(propertyUri);
        String propertyName = getPropertyName(propertyUri);
        Property prop = ResourceFactory.createProperty(propertyURI, propertyName);
        ResIterator iter = model.listResourcesWithProperty(prop);
        List<ResourceApi> resources = new ArrayList<>();
        while(iter.hasNext()){
            Resource resource = iter.nextResource();
            StmtIterator stmt = resource.listProperties();
            while(stmt.hasNext()){
                Statement triple = stmt.nextStatement();
                Property predicate = triple.getPredicate();
                RDFNode object = triple.getObject();
                boolean propertyHasValue = (isExactly)? object.toString().equals(value) : object.toString().contains(value);
                if( propertyHasValue && predicate.equals(prop)){
                    ResourceApi resourceApi = getResourceApi(workspace, resource.getURI());
                    if (resourceApi != null) resources.add(resourceApi);
                } else if (object instanceof Resource && !object.toString().contains("http://")){
                    StmtIterator subIter = ((Resource) object).listProperties();
                    while(subIter.hasNext()){
                        Statement subTriple = subIter.nextStatement();
                        Property subProp = subTriple.getPredicate();
                        RDFNode subObject = subTriple.getObject();
                        boolean subpropertyHasValue = (isExactly)? subObject.toString().equals(value) : subObject.toString().contains(value);
                        if(subpropertyHasValue && subProp.equals(prop)) {
                            ResourceApi resourceApi = getResourceApi(workspace, resource.getURI());
                            if (resourceApi != null) resources.add(resourceApi);
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok(resources);
    }


    /**
     * Method responsible for retrieving predicates of vocabulary of interest according to the given term
     * @param vocabPrefix String with the prefix of the vocabulary in which the searches will be done
     * @param search String with the search term to find properties with similarity to the given term
     * @return Returns an HTTP status 'ok' with an OptGroup list. Each of these contains specific predicates of that group defined in the vocabulary.
     * <br> An example is Schema.org which has Product, Place, Person, Organization, Review, Action ...
     */
    @GetMapping("/getVocabularyData")
    @ApiOperation(value = "Method responsible for retrieving predicates of vocabulary of interest according to the given term", response = OptGroup.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the predicate list successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    public ResponseEntity<Collection<OptGroup>> getVocabularyPredicates(
            @RequestParam(value = "vocabPrefix", required = false) String vocabPrefix,
            @RequestParam(value = "search", required = false) String search){

        List<OptGroup> predicates = new ArrayList<>();
        if (!hasText(vocabPrefix) || !hasText(search))
            return ResponseEntity.ok(predicates);

        KnownVocabs knownVocab = KnownVocabs.valueOf(vocabPrefix.toUpperCase());

        OptGroupBuilder optGroupBuilder = new OptGroupBuilder();
        Map<String, OptGroup> optGroupMap = optGroupBuilder.build(knownVocab.getUri(), knownVocab.getFilePath(), search);

        return ResponseEntity.ok(optGroupMap.values());
    }

    /**
     * Method for obtaining a resource given his URI
     * @param workspace String to hold the graph nome that contain the resource searched
     * @param resourceId String to hold the resource ID searched
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @GetMapping("/getResource/{workspace}")
    @ApiOperation(value = "Method responsible for obtaining a resource given his uri", response = ResponseEntity.class)
    public ResponseEntity<ResourceApi> getResource(@PathVariable String workspace, @RequestParam String resourceId) {

        ResourceApi resource = getResourceApi(workspace, resourceId);

        return ResponseEntity.ok(resource);
    }

    /**
     * Method responsible for obtaining a list of resources given the resource type
     * @param workspaces A string list that hold the graphs names that contain the resource searched
     * @param type String to hold the resource type
     * @return Returns a ResponseEntity containing a APIResponse
     */
    @GetMapping("/getResourcesByType")
    @ApiOperation(value = "Method responsible for obtaining a list of resources given the resource type", response = ResponseEntity.class)
    public ResponseEntity<List<ResourceApi>> getResourcesByType(@RequestParam List<String> workspaces, @RequestParam String type){
        List<ResourceApi> resourcesApi = new ArrayList<>();

        for (String workspace:  workspaces) {
            graphURI = (workspace.charAt(0) == '/')? workspace : "/" + workspace;
            Model model = datasetAccessor.getModel(graphURI);

            ResIterator resIter = model.listSubjects();
            List<Resource> resources = new ArrayList<>();
            while(resIter.hasNext()){
                Resource resource = resIter.nextResource();
                try{
                    if (resource.getURI().contains("http://"))
                        resources.add(resource);
                }catch (Exception e){
                }
            }
            for(Resource resource: resources){
                if (getResourceTypeName(resource).equals(type)){
                    ResourceApi r =  getResourceApi(workspace, resource.getURI());
                    resourcesApi.add(r);
                }
            }

        }
        return ResponseEntity.ok(resourcesApi);
    }

    public ResourceApi getResourceApi(String workspace, String resourceId) {

        ResourceApi resource = new ResourceApi();

        if(workspace != null && resourceId != null){
            String graphURI = (workspace.charAt(0) == '/') ? workspace : ("/" + workspace);

            resourceId = resourceId.replace("|", "/");
            resourceId = resourceId.replace("_", ".");
            Model model = datasetAccessor.getModel(graphURI);
            if (model == null) return null;
            Resource r = model.getResource(resourceId);
            if (r == null) return null;

            int uriSize             = r.getURI().length();
            int resourceNameSize    = getResourceNameSize(r);
            String resourcePrefix   = r.getURI().substring(0, uriSize - resourceNameSize);
            resource.setPrefix( model.getNsURIPrefix(resourcePrefix) );
            resource.setName(getResourceTypeName(r));
            resource.setAbout(r.getURI());

            List<PrefixedPair> prefixedPairs = getPrefixedPairs(r);

            List<Vocabulary> vocabularies = getVocabularies(prefixedPairs);

            resource.setVocabularies(vocabularies);
        }

        return resource;
    }

    private int getResourceNameSize(Resource resource){
        int resourceNameSize = 0;
        String resourceUri = resource.getURI();
        for (int x = resourceUri.length(); resourceUri.charAt(x - 1) != '/'; x--)
            resourceNameSize++;
        return resourceNameSize;
    }

    private void showStatements(StmtIterator iter2) {
        while (iter2.hasNext()){
            Statement stmt      = iter2.nextStatement();  // get next statement
            Resource subject    = stmt.getSubject();     // get the subject
            Property predicate  = stmt.getPredicate();   // get the predicate
            RDFNode object      = stmt.getObject();      // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                StmtIterator subiter = ((Resource) object).listProperties();
                while(subiter.hasNext()){
                    Statement subStmt = subiter.nextStatement();
                    Resource subsubject     = subStmt.getSubject();     // get the subject
                    Property subpredicate   = subStmt.getPredicate();   // get the predicate
                    RDFNode subobject       = subStmt.getObject();      // get the object
                    System.out.println(subsubject.toString());
                    System.out.print(" " + subpredicate.toString() + " ");
                    System.out.print(" \"" + subobject.toString() + "\"");
                }
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }

            System.out.println(" .");
        }
    }

    public String getResourceTypeName(Resource resource) {
        StmtIterator iter = resource.listProperties();
        String resourceTypeName = null;
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();
            RDFNode object      = stmt.getObject();
            if (stmt.getPredicate().getLocalName().equals("type")) {
                String typeUri  = ((Resource) object).getURI();
                resourceTypeName    = getPropertyName(typeUri);
                break;
            }
        }
        return resourceTypeName;
    }

    public String getPropertyName(String propertyUri) {
         int propSize = 0;
         for(int i = propertyUri.length() - 1; propertyUri.charAt(i) != '/' && propertyUri.charAt(i) != '#'; i--)
             propSize++;
         String propertyNameUri = propertyUri.substring(propertyUri.length() - propSize, propertyUri.length());
         return propertyNameUri;
    }

    public String getPropertyNameSpace(String propertyUri) {
        int propSize = 0;
        for(int i = propertyUri.length() - 1; propertyUri.charAt(i) != '/' && propertyUri.charAt(i) != '#'; i--)
            propSize++;
        String propertyNameSpace = propertyUri.substring(0, propertyUri.length() - propSize);
        return propertyNameSpace;
    }


    private List<Vocabulary> getVocabularies(List<PrefixedPair> prefixedPairs) {
        List <PrefixedPair> usedVocabularies = getUsedVocabularies(prefixedPairs);
        List <Vocabulary> vocabularies = new ArrayList<>();
        for (PrefixedPair vocab: usedVocabularies){
            List<Pair> pairsForVocab = null;
            Vocabulary vocabulary = null;
            if (!vocabularyExists(vocabularies, vocab)){
                vocabulary = new Vocabulary();
                pairsForVocab =  new ArrayList<>();
                vocabulary.setPrefix(vocab.getPrefix());
                vocabulary.setUri(vocab.getUri());
                for (PrefixedPair prefixedPair: prefixedPairs) {
                    String vocabURI1 = vocabulary.getUri().substring(0, vocabulary.getUri().length() - vocabulary.getPrefix().length());
                    String vocabURI2 = prefixedPair.getUri().substring(0, prefixedPair.getUri().length() - prefixedPair.getPrefix().length());
                    if (vocabURI1.equals(vocabURI2))
                        pairsForVocab.add(prefixedPair.getPair());
                }
                vocabulary.setPairs(pairsForVocab);
                vocabularies.add(vocabulary);
            }
        }
        return vocabularies;
    }

    private List<PrefixedPair> getPrefixedPairs(Resource r) {
        List<Pair> pairs = new ArrayList<>();
        List<PrefixedPair> prefixedPairs = new ArrayList<>();
        StmtIterator propIter = r.listProperties();                                         //Pega todas as propriedades do recurso
        while (propIter.hasNext()) {                                                        //Enquanto tiver próxima propriedade
            Statement propStmt  = propIter.nextStatement();                                 //Pega a proxima propriedades
            Property prop       = propStmt.getPredicate();
            RDFNode object      = propStmt.getObject();                                     //


            String vocabURI = prop.getURI().substring(0, prop.getURI().length() - prop.getLocalName().length());
            String URIPrefix = r.getModel().getNsURIPrefix(vocabURI);

            if((object instanceof Resource && !prop.getLocalName().equals("type")) && !object.toString().contains("http://")) {
                Pair pairAsResource = new Pair(prop.getLocalName(), "", true, "");
                prefixedPairs.add(new PrefixedPair(URIPrefix, vocabURI, pairAsResource));

                pairs.add(pairAsResource);
            }

            if(!prop.getLocalName().equals("type")) {
                if (object instanceof Resource && !object.toString().contains("http://")) {
                    StmtIterator subpropIter = ((Resource) object).listProperties();
                    while (subpropIter.hasNext()) {
                        Statement subpropSmtm = subpropIter.nextStatement();
                        Property subp = subpropSmtm.getPredicate();
                        RDFNode subObjet = subpropSmtm.getObject();
                        if (subObjet.isResource()) {
                            Pair pair = new Pair(subp.getLocalName(), subObjet.toString(), true, prop.getLocalName());
                            prefixedPairs.add(new PrefixedPair(URIPrefix, vocabURI, pair));
                            pairs.add(pair);
                        } else {
                            Pair pair = new Pair(subp.getLocalName(), subObjet.toString(), false, prop.getLocalName());
                            prefixedPairs.add(new PrefixedPair(URIPrefix, vocabURI, pair));
                            pairs.add(pair);
                        }
                    }
                } else {
                    if (object.isResource()) {
                        Pair pair = new Pair(prop.getLocalName(), object.toString(), true, "");
                        prefixedPairs.add(new PrefixedPair(URIPrefix, vocabURI, pair));
                        pairs.add(pair);
                    } else {
                        Pair pair = new Pair(prop.getLocalName(), object.toString(), false, "");
                        prefixedPairs.add(new PrefixedPair( URIPrefix, vocabURI, pair));
                        pairs.add(pair);
                    }
                }
            }
        }
        return prefixedPairs;
    }

    private boolean vocabularyExists(List<Vocabulary> vocabularies, PrefixedPair prefixedPair){
        for(Vocabulary vocabulary: vocabularies){
            String vocabURI1 = vocabulary.getUri().substring(0, vocabulary.getUri().length() - vocabulary.getPrefix().length());
            String vocabURI2 = prefixedPair.getUri().substring(0, prefixedPair.getUri().length() - prefixedPair.getPrefix().length());
            if(vocabURI1.equals(vocabURI2)){
                return true;
            }
        }
        return false;
    }

    public List<PrefixedPair> getUsedVocabularies(List<PrefixedPair> prefixedPairs){

        List<PrefixedPair> usedVocabularies = new ArrayList();
        for (PrefixedPair prefixedPair: prefixedPairs){
            if (!usedVocabularies.contains(prefixedPair.getPrefix())){
                usedVocabularies.add(prefixedPair);
            }
        }
        return usedVocabularies;
    }

    /**
     * Method responsible for creating a new resource according to the data passed according to the predefined ontology
     * <br> The vocabularies, properties and values will be passed to create the new instance of the ontology
     * @param model Model that will receive the data of the new ontology to be created
     * @param resource A ResourceApi with data from the new ontology
     */
    public void addAsResource(Model model, ResourceApi resource) {
        Resource resourceDefiniton = ResourceFactory
                .createResource(getResourceUri(resource) +  resource.getName());
        Resource resourceInstance = model.createResource(getResourceID(resource), resourceDefiniton);

        for (Vocabulary v : resource.getVocabularies()) {
            ArrayList<Verified> verifiedPairs = new ArrayList<>();
            for(Pair p : v.getPairs()){
                verifiedPairs.add(new Verified(p));
            }
            for (int i = 0; i < verifiedPairs.size(); i++) {
                String propertyName = verifiedPairs.get(i).getPair().getPropertyName();
                //Represents the property like a a comom tag: <vocab:property>value</vocab:property>
                //If the pair was'nt verified yet and the value of his property isn't empty and the value of his property not contais "http://" and is a resource and the value of the subproperty is empty
                if (!verifiedPairs.get(i).isVerified()
                        && !verifiedPairs.get(i).getPair().getValue().isEmpty()
                        && !verifiedPairs.get(i).getPair().getValue().contains("http://")
                        && !verifiedPairs.get(i).getPair().isAsResource()
                        && verifiedPairs.get(i).getPair().getSubPropertyOf().isEmpty()){

                    Property property = ResourceFactory.createProperty(normalizeURI(v.getUri()), propertyName);
                    resourceInstance.addProperty(property, verifiedPairs.get(i).getPair().getValue());
                    verifiedPairs.get(i).setVerified(true);
                }
                //Represents the property that have one simple resource: <vocab:property rdf:resource="http://site.com"/>
                //If the pair was'nt verified yet and the value of his property contais "http://" and is a resource and the value of the subproperty is empty
                else if(!verifiedPairs.get(i).isVerified()
                        && verifiedPairs.get(i).getPair().getValue().contains("http://")
                        && verifiedPairs.get(i).getPair().isAsResource()
                        && verifiedPairs.get(i).getPair().getSubPropertyOf().isEmpty()){

                    Resource propResource = ResourceFactory
                            .createResource(verifiedPairs.get(i).getPair().getValue());
                    Property property = ResourceFactory.createProperty(normalizeURI(v.getUri()), propertyName);
                    model.add(resourceInstance, property, propResource);
                    verifiedPairs.get(i).setVerified(true);
                }
                //Represents the begins of a resource: <vocab:property rdf:parseType="Resource">
                //If the pair was'nt verified yet and the value of his property is empty and is a resource and the value of the subproperty is empty
                else if (!verifiedPairs.get(i).isVerified()
                        && verifiedPairs.get(i).getPair().getValue().isEmpty()
                        && verifiedPairs.get(i).getPair().isAsResource()
                        && verifiedPairs.get(i).getPair().getSubPropertyOf().isEmpty()) {
                    Property property = ResourceFactory.createProperty(normalizeURI(v.getUri()), propertyName);
//                    RDFNode innerNode =
                    Resource innerResource = model.createResource();
                    List<Pair> pairs = v.getPairs();
                    for (int k = 0; k < v.getPairs().size(); k++) {
                        //Treats the internal properties of the new resource included
                        //If the name of property of one pair is equals to the property name of another pair
                        if (verifiedPairs.get(i).getPair().getPropertyName().equals(pairs.get(k).getSubPropertyOf())) {
                            //Treats the case wich the subproperty is represented like a resource <vocab:property rdf:resource="http://anything.com"/
                            if(!pairs.get(k).getValue().equals("") && pairs.get(k).isAsResource()){
                                Property innerProperty = ResourceFactory.createProperty(normalizeURI(v.getUri()), pairs.get(k).getPropertyName());
                                Resource r = ResourceFactory.createResource(pairs.get(k).getValue());
                                model.add(innerResource, innerProperty, r);
                            }
                            //Treats the case wich the subproperty is represented like a comom tag: <vocab:property>value</vocab:property>
                            else{
                                Property innerProperty = ResourceFactory
                                        .createProperty(normalizeURI(v.getUri()), pairs.get(k).getPropertyName());
                                innerResource.addProperty(innerProperty, pairs.get(k).getValue());
                            }
                            verifiedPairs.get(k).setVerified(true);
                        }
                    }
                    resourceInstance.addProperty(property, innerResource);
                    verifiedPairs.get(i).setVerified(true);
                }
            }
        }
    }

    /**
     * Method responsible for normalizing the URI of the created resource by adding '/' if necessary
     * @param uri String that stores the URI
     * @return Returns a String from a normalized URI
     */
    public String normalizeURI(String uri) {
        return (uri.endsWith("/") || uri.endsWith("#")) ? uri : uri + "/";
    }

    /**
     * Method that generates a unique identifier to be coupled to the URI for a new instance of the ontology
     * @param resource ResourceApi resource passed to treat resource URI
     * @return Returns a String with the resource URI plus a unique identifier
     */
//    private String getResourceID(ResourceApi resource) {
//        return resource.getAbout() + UUID.randomUUID();
//    }
    private String getResourceID(ResourceApi resource) {
        boolean hasLocalName = resourceHasLocalName(resource);
        String localName =  hasLocalName? getResourceLocalName(resource) : String.valueOf(UUID.randomUUID());
        return getResourceUri(resource) + localName;
    }

    private boolean resourceHasLocalName(ResourceApi resource) {
        int localNameSize = getResourceLocalNameSize(resource);
        return (localNameSize == 37 && (!getResourceLocalName(resource).contains(".")));
    }

    private int getResourceLocalNameSize(ResourceApi resource) {
        int localNameSize = 0;
        String aboutResource = resource.getAbout();
        for (int x = aboutResource.length() - 2; aboutResource.charAt(x) != '/'; x--)
            localNameSize++;
        localNameSize++ ;
        return localNameSize;
    }

    private String getResourceLocalName(ResourceApi resource){
        String resourceLocalName;
        String resourceAbout = resource.getAbout();
        resourceLocalName = resourceAbout.substring(resource.getAbout().length() - getResourceLocalNameSize(resource), resourceAbout.length() - 1);
        return resourceLocalName;
    }

    private  String getResourceUri(ResourceApi resource){
        String resourceUri;
        String aboutResource = resource.getAbout();
        resourceUri = resourceHasLocalName(resource) ? aboutResource.substring(0, aboutResource.length() - getResourceLocalNameSize(resource)) : aboutResource;
        return resourceUri;
    }


    /**
     * Method responsible for creating a template that will receive the given data:
     * <br> Vocabularies with their respective prefixes and properties with their values
     * @param resource ResourceApi with the data to insert into the template
     * @return Returns a Model with the passed data
     */
    public Model createModel(ResourceApi resource) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix(resource.getPrefix(), getResourceUri(resource));
        for (Vocabulary v : resource.getVocabularies()) {
            model.setNsPrefix(v.getPrefix(), normalizeURI(v.getUri()));
        }
        return model;
    }
}
