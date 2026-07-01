package com.gis.sampling_backend.entity;

public class SamplingPointDto {
    private String name;
    private String type;
    private String description;
    private String collector;
    private String collectTime;  // 变量名是 collectTime，不是 String
    private Double lng;
    private Double lat;

    // 无参构造
    public SamplingPointDto() {
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getCollector() {
        return collector;
    }

    public String getCollectTime() {
        return collectTime;
    }

    public Double getLng() {
        return lng;
    }

    public Double getLat() {
        return lat;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCollector(String collector) {
        this.collector = collector;
    }

    public void setCollectTime(String collectTime) {
        this.collectTime = collectTime;  // 注意：this.collectTime = collectTime
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}