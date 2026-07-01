package com.gis.sampling_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Entity
@Table(name = "sampling_points")
public class SamplingPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String description;
    private String collector;
    private LocalDateTime collectTime;

    @Column(columnDefinition = "geometry(Point, 4326)")
    @JsonSerialize(using = ToStringSerializer.class)  // 添加这行，把 geometry 转为字符串
    private Point location;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCollector() { return collector; }
    public void setCollector(String collector) { this.collector = collector; }

    public LocalDateTime getCollectTime() { return collectTime; }
    public void setCollectTime(LocalDateTime collectTime) { this.collectTime = collectTime; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
}