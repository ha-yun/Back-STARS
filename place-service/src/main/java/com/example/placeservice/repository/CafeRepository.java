package com.example.placeservice.repository;

import com.example.placeservice.entity.Accommodation;
import com.example.placeservice.entity.Cafe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    @Query("SELECT c FROM Cafe c WHERE c.area.areaId = :areaId")
    List<Cafe> findByAreaId(@org.springframework.data.repository.query.Param("areaId") Long areaId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Cafe c WHERE c.area.areaId = :areaId")
    void deleteByAreaId(Long areaId);

    Optional<Cafe> findByKakaomapUrl(String placeCode);
    List<Cafe> findByNameContainingIgnoreCase(String keyword);
    List<Cafe> findByAddressContainingIgnoreCase(String keyword);

    // 성능 개선을 위한 추가 메소드
    @EntityGraph(attributePaths = {"area"})
    List<Cafe> findAll();

    @EntityGraph(attributePaths = {"area"})
    Optional<Cafe> findById(Long id);
}