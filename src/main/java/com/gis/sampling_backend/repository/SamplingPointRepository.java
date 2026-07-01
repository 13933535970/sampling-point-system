package com.gis.sampling_backend.repository;

import com.gis.sampling_backend.entity.SamplingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SamplingPointRepository extends JpaRepository<SamplingPoint, Long> {

    List<SamplingPoint> findByType(String type);

    // 搜索接口（必须存在）
    @Query("SELECT p FROM SamplingPoint p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SamplingPoint> searchByName(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM sampling_points WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :radius)", nativeQuery = true)
    List<SamplingPoint> findWithinRadius(@Param("lng") double lng,
                                         @Param("lat") double lat,
                                         @Param("radius") double radius);
}