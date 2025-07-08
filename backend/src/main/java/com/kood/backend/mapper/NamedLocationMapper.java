package com.kood.backend.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.kood.backend.dto.LocationDTOs.LocationPoint;
import com.kood.backend.dto.LocationDTOs.NamedLocationDTO;
import com.kood.backend.entity.LocationEntities.NamedLocation;

public interface NamedLocationMapper {
    public static NamedLocationDTO toDTO(NamedLocation entity) {
        if (entity == null) {
            return null;
        }
        LocationPoint point = new LocationPoint(0, 0);
        point.setLongitude(entity.getPoint().getX());
        point.setLatitude(entity.getPoint().getY());

        return NamedLocationDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .point(point)
                .accuracy(entity.getAccuracy())
                .build();
    }

    public static NamedLocation toEntity(NamedLocationDTO namedLocationDTO) {
        if (namedLocationDTO == null) {
            return null;
        }
        NamedLocation location = new NamedLocation();
        location.setId(namedLocationDTO.getId());
        LocationPoint coords = namedLocationDTO.getPoint();
        Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
        Point point = new GeometryFactory().createPoint(coordinate);
        point.setSRID(4326);
        location.setPoint(point);
        location.setAccuracy(namedLocationDTO.getAccuracy());
        location.setName(namedLocationDTO.getName());
        return location;
    }

}
