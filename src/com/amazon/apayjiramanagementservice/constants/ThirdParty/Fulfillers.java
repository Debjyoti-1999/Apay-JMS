package com.amazon.apayjiramanagementservice.constants.ThirdParty;


import java.util.List;

public enum Fulfillers {
    /** for assigneeID BillDesk: https://home.atlassian.com/o/cbe86080-4a5c-41c7-82e4-887152580523/people/
     712020:ceffafd5-7388-42e1-8dbd-5977ae92d015?cloudId=c7a9af2a-df16-46fe-bc4a-6ca32cdd62a1

     for assigneeID Euronet: https://home.atlassian.com/o/
     cbe86080-4a5c-41c7-82e4-887152580523/people/624cb3c98678e900705e5c67?cloudId=c7a9af2a-df16-46fe-bc4a-6ca32cdd62a1

     // for project ID (Euronet) call API: <a href="https://amazon-india-payments.atlassian.net/rest/api/latest/project/HFCEN">...</a>

     // for project ID (BillDesk) call API: <a href="https://amazon-india-payments.atlassian.net/rest/api/latest/project/HFCBP">...</a>
     **/

    BILLDESK("BILLDESK", "712020:ceffafd5-7388-42e1-8dbd-5977ae92d015",
            "10006", "Sev 2", List.of("automated-ticket")), // Ananya Khatri


    EURONET("EURONET", "624cb3c98678e900705e5c67", "10011", "Sev 2",
            List.of("automated-ticket")); //Sumita Sadhu

    private final String name;
    private final String assigneeID;
    private final String projectID;
    private final String priorityName;
    private final List<String> labels;

    Fulfillers(String name, String assigneeID, String projectID, String priorityName, List<String> labels) {
        this.name = name;
        this.assigneeID = assigneeID;
        this.projectID = projectID;
        this.priorityName = priorityName;
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public String getAssigneeID() {
        return assigneeID;
    }

    public String getProjectID() {
        return projectID;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public List<String> getLabels() {
        return labels;
    }
}
