package uk.gov.hmcts.sptribs.consumer.bundle;

public class BundleFolder {
    private String name;
    private Integer sortIndex;  // Assuming sortIndex is an integer field, adjust as needed

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }
}
