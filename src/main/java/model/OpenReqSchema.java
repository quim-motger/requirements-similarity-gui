package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenReqSchema implements Serializable {

    private List<OpenReqRequirement> requirements;
    private List<OpenReqProject> projects;
    private List<OpenReqDependency> dependencies;

    public OpenReqSchema() {

    }

    public List<OpenReqRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<OpenReqRequirement> requirements) {
        this.requirements = requirements;
    }

    public List<OpenReqProject> getProjects() {
        return projects;
    }

    public void setProjects(List<OpenReqProject> projects) {
        this.projects = projects;
    }

    public List<OpenReqDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<OpenReqDependency> dependencies) {
        this.dependencies = dependencies;
    }

}
