package sv.edu.catolica.taskflow.models;

import java.util.List;
import java.util.Map;

public class ProjectStatsResponse {
    private int total_projects;
    private int total_tasks;
    private Map<String, Integer> tasks_by_estado;
    private double percent_completed_overall;
    private List<Proyecto> projects;
    private List<Proyecto> top_projects_by_completion;
    private Map<String, Integer> tasks_per_month;

    public ProjectStatsResponse() {}

    // Getters and setters
    public int getTotal_projects() { return total_projects; }
    public void setTotal_projects(int total_projects) { this.total_projects = total_projects; }

    public int getTotal_tasks() { return total_tasks; }
    public void setTotal_tasks(int total_tasks) { this.total_tasks = total_tasks; }

    public Map<String, Integer> getTasks_by_estado() { return tasks_by_estado; }
    public void setTasks_by_estado(Map<String, Integer> tasks_by_estado) { this.tasks_by_estado = tasks_by_estado; }

    public double getPercent_completed_overall() { return percent_completed_overall; }
    public void setPercent_completed_overall(double percent_completed_overall) { this.percent_completed_overall = percent_completed_overall; }

    public List<Proyecto> getProjects() { return projects; }
    public void setProjects(List<Proyecto> projects) { this.projects = projects; }

    public List<Proyecto> getTop_projects_by_completion() { return top_projects_by_completion; }
    public void setTop_projects_by_completion(List<Proyecto> top_projects_by_completion) { this.top_projects_by_completion = top_projects_by_completion; }

    public Map<String, Integer> getTasks_per_month() { return tasks_per_month; }
    public void setTasks_per_month(Map<String, Integer> tasks_per_month) { this.tasks_per_month = tasks_per_month; }
}
