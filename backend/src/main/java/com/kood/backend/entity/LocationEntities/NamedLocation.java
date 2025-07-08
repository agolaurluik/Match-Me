package com.kood.backend.entity.LocationEntities;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "named_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NamedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point point;
    // 4326 — this
    // is the SRID (Spatial Reference System Identifier), specifically:
    // 4326 = WGS 84, the standard used by GPS and browsers.
    // It ensures spatial functions (like distance, proximity, etc.) behave
    // correctly on Earth coordinates.

    private double accuracy;

}
