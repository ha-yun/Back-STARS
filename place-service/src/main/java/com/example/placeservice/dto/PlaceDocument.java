package com.example.placeservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class PlaceDocument {

    // 공통
    private String name;
    private String type;
    private String address;
    private String place_id;
    private BigDecimal lat;
    private BigDecimal lon;
    private String phone;
    private String kakaomap_url;

}
