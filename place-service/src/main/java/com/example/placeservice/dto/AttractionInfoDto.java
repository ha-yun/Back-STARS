package com.example.placeservice.dto;

import com.example.placeservice.entity.Attraction;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttractionInfoDto {
    private Long attraction_id;
    private String name;
    private String address;
    private BigDecimal lat;
    private BigDecimal lon;
    private String phone;
    private String homepage_url;
    private String close_day;
    private String use_time;

    public AttractionInfoDto(Long attractionId, String name, String address, BigDecimal lat, BigDecimal lon, String phone, String homepage_url, String close_day, String use_time) {
        this.attraction_id = attractionId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.phone = phone;
        this.homepage_url = homepage_url;
        this.close_day = close_day;
        this.use_time = use_time;
    }

}
