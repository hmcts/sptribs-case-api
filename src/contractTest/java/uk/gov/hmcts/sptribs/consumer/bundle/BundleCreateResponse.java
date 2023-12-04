package uk.gov.hmcts.sptribs.consumer.bundle;

public class BundleCreateResponse {

    private String id;
    private java.util.List<CaseBundle> caseBundles;

    public BundleCreateResponse(String s) {

    }

    public String getId() {
        return id;
    }

    public java.util.List<CaseBundle> getCaseBundles() {
        return caseBundles;
    }

    public void setCaseBundles(java.util.List<CaseBundle> caseBundles) {
        this.caseBundles = caseBundles;
    }

}






