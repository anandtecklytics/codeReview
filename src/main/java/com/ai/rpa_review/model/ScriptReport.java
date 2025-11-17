package com.ai.rpa_review.model;

import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public class ScriptReport {

    private String tool;
    private int compilation_score;
    private List<String> issues;
    private List<String> recommendation;

    public String getTool() {
        return tool;
    }

    public int getCompilation_score() {
        return compilation_score;
    }

    public List<String> getIssues() {
        return issues;
    }

    public List<String> getRecommendation() {
        return recommendation;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setCompilation_score(int compilation_score) {
        this.compilation_score = compilation_score;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public void setRecommendation(List<String> recommendation) {
        this.recommendation = recommendation;
    }
}
