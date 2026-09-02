package com.lineacademy.shelterbackendspring.util;

import org.locationtech.proj4j.*;
import org.springframework.stereotype.Component;

@Component
public class CoordinateTransformUtil {

    private final CoordinateTransform transform;

    public CoordinateTransformUtil() {
        CRSFactory crsFactory = new CRSFactory();

        // 1. 서울시 중부원점 TM 파라미터 (EPSG:2097 / 5181 계열)
        CoordinateReferenceSystem srcCrs = crsFactory.createFromParameters(
                "EPSG:2097",
                "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs"
        );

        // 2. 표준 WGS84 위경도 파라미터 (EPSG:4326) 직접 등록 (파일 참조 에러 방지)
        CoordinateReferenceSystem dstCrs = crsFactory.createFromParameters(
                "EPSG:4326",
                "+proj=longlat +datum=WGS84 +no_defs"
        );

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        this.transform = ctFactory.createTransform(srcCrs, dstCrs);
    }

    /**
     * TM X, Y 좌표를 받아 [위도(lat), 경도(lng)] 배열로 반환
     */
    public double[] transformTmToWgs84(double x, double y) {
        ProjCoordinate srcCoord = new ProjCoordinate(x, y);
        ProjCoordinate dstCoord = new ProjCoordinate();

        transform.transform(srcCoord, dstCoord);

        // dstCoord.y = 위도(Latitude), dstCoord.x = 경도(Longitude)
        return new double[]{dstCoord.y, dstCoord.x};
    }
}
