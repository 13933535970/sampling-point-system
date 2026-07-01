package com.gis.sampling_backend.controller;

import com.gis.sampling_backend.entity.SamplingPoint;
import com.gis.sampling_backend.entity.SamplingPointDto;
import com.gis.sampling_backend.service.SamplingPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SamplingPointController {

    private static final Logger log = LoggerFactory.getLogger(SamplingPointController.class);

    @Autowired
    private SamplingPointService service;

    @GetMapping
    public List<SamplingPoint> getAll() {
        log.info("GET /api/points");
        return service.findAll();
    }

    @PostMapping
    public SamplingPoint create(@RequestBody SamplingPointDto dto) {
        log.info("POST /api/points - 接收数据: name={}", dto.getName());
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public SamplingPoint update(@PathVariable Long id, @RequestBody SamplingPointDto dto) {
        log.info("PUT /api/points/{}", id);
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE /api/points/{}", id);
        service.delete(id);
    }

    @GetMapping("/filter")
    public List<SamplingPoint> filterByType(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return service.findByType(type);
        }
        return service.findAll();
    }

    @GetMapping("/spatial/radius")
    public List<SamplingPoint> findByRadius(@RequestParam double lng,
                                            @RequestParam double lat,
                                            @RequestParam double radius) {
        return service.findWithinRadius(lng, lat, radius);
    }

    @GetMapping("/search")
    public List<SamplingPoint> searchByName(@RequestParam String keyword) {
        System.out.println("搜索关键词：" + keyword);
        return service.searchByName(keyword);
    }
}