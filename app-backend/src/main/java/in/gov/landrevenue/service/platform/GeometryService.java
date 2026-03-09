package in.gov.landrevenue.service.platform;

import in.gov.landrevenue.model.platform.GeoPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeometryService {
    public boolean polygonsOverlap(List<GeoPoint> a, List<GeoPoint> b) {
        for (int i = 0; i < a.size(); i++) {
            GeoPoint a1 = a.get(i);
            GeoPoint a2 = a.get((i + 1) % a.size());
            for (int j = 0; j < b.size(); j++) {
                GeoPoint b1 = b.get(j);
                GeoPoint b2 = b.get((j + 1) % b.size());
                if (segmentsIntersect(a1, a2, b1, b2)) {
                    return true;
                }
            }
        }
        return pointInPolygon(a.get(0), b) || pointInPolygon(b.get(0), a);
    }

    private boolean segmentsIntersect(GeoPoint p1, GeoPoint q1, GeoPoint p2, GeoPoint q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);
        return o1 != o2 && o3 != o4;
    }

    private int orientation(GeoPoint p, GeoPoint q, GeoPoint r) {
        double val = (q.lng() - p.lng()) * (r.lat() - q.lat()) - (q.lat() - p.lat()) * (r.lng() - q.lng());
        if (Math.abs(val) < 1e-9) {
            return 0;
        }
        return (val > 0) ? 1 : 2;
    }

    private boolean pointInPolygon(GeoPoint point, List<GeoPoint> polygon) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            GeoPoint pi = polygon.get(i);
            GeoPoint pj = polygon.get(j);
            boolean intersect = ((pi.lng() > point.lng()) != (pj.lng() > point.lng())) &&
                    (point.lat() < (pj.lat() - pi.lat()) * (point.lng() - pi.lng()) / (pj.lng() - pi.lng() + 1e-9) + pi.lat());
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }
}
