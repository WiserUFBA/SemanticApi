package com.eudes.semanticMake.framework;

/** Config has variables with values needed to the inner methods operation
 * Created by Eudes on 16/05/2018.
 */
public class SMConfig {

    private String baseURL;

    private String datasetAddress;

    private String datasetName;

    private String workspace;

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetAddress() {
        return datasetAddress;
    }

    public void setDatasetAddress(String datasetAddress) {
        this.datasetAddress = datasetAddress;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
}
