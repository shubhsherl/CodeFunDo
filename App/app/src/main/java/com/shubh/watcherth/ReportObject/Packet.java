package com.shubh.watcherth.ReportObject;

import java.util.List;

public class Packet{
    List<String> path;
    String token;

    public Packet(){}

    public Packet(List path, String token){
        this.path = path;
        this.token = token;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}