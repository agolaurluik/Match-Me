package com.kood.backend.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.kood.backend.dto.LocationDTOs.LocationDTO;
import com.kood.backend.dto.LocationDTOs.LocationPoint;
import com.kood.backend.entity.LocationEntities.Location;

public interface LocationMapper {
    public static LocationDTO toDTO(Location entity) {
        if (entity == null) {
            return null;
        }
        LocationPoint point = new LocationPoint(0, 0);
        point.setLongitude(entity.getPoint().getX());
        point.setLatitude(entity.getPoint().getY());

        return LocationDTO.builder()
                .id(entity.getId())
                .point(point)
                .accuracy(entity.getAccuracy())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static Location toEntity(LocationDTO locationDTO) {
        if (locationDTO == null) {
            return null;
        }
        Location location = new Location();
        location.setId(locationDTO.getId());
        LocationPoint coords = locationDTO.getPoint();
        Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude());
        Point point = new GeometryFactory().createPoint(coordinate);
        point.setSRID(4326);
        location.setPoint(point);
        location.setAccuracy(locationDTO.getAccuracy());
        location.setTimestamp(locationDTO.getTimestamp());
        return location;
    }

}
