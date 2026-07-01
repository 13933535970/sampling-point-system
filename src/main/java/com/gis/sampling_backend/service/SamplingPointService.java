package com.gis.sampling_backend.service;

import com.gis.sampling_backend.entity.SamplingPoint;
import com.gis.sampling_backend.entity.SamplingPointDto;
import com.gis.sampling_backend.repository.SamplingPointRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SamplingPointService {

    private static final Logger log = LoggerFactory.getLogger(SamplingPointService.class);

    @Autowired
    private SamplingPointRepository repository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<SamplingPoint> findAll() {
        return repository.findAll();
    }

    public List<SamplingPoint> findByType(String type) {
        return repository.findByType(type);
    }

    public List<SamplingPoint> findWithinRadius(double lng, double lat, double radius) {
        return repository.findWithinRadius(lng, lat, radius);
    }

    // ===== 新增：按名称搜索 =====
    public List<SamplingPoint> searchByName(String keyword) {
        log.info("搜索采样点: keyword={}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAll();
        }
        return repository.searchByName(keyword.trim());
    }

    public SamplingPoint create(SamplingPointDto dto) {
        log.info("创建采样点: name={}, lng={}, lat={}", dto.getName(), dto.getLng(), dto.getLat());

        SamplingPoint point = new SamplingPoint();
        point.setName(dto.getName());
        point.setType(dto.getType());
        point.setDescription(dto.getDescription());
        point.setCollector(dto.getCollector());

        String collectTimeStr = dto.getCollectTime();
        if (collectTimeStr != null && !collectTimeStr.trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(collectTimeStr, formatter);
                point.setCollectTime(dateTime);
            } catch (Exception e) {
                point.setCollectTime(LocalDateTime.now());
            }
        } else {
            point.setCollectTime(LocalDateTime.now());
        }

        Point location = geometryFactory.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
        location.setSRID(4326);
        point.setLocation(location);

        return repository.save(point);
    }

    public SamplingPoint update(Long id, SamplingPointDto dto) {
        SamplingPoint point = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found: " + id));

        point.setName(dto.getName());
        point.setType(dto.getType());
        point.setDescription(dto.getDescription());
        point.setCollector(dto.getCollector());

        String collectTimeStr = dto.getCollectTime();
        if (collectTimeStr != null && !collectTimeStr.trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(collectTimeStr, formatter);
                point.setCollectTime(dateTime);
            } catch (Exception e) {
                point.setCollectTime(LocalDateTime.now());
            }
        }

        if (dto.getLng() != null && dto.getLat() != null) {
            Point location = geometryFactory.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
            location.setSRID(4326);
            point.setLocation(location);
        }

        return repository.save(point);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}