import { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polygon, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet's broken default icon paths in Vite/webpack builds
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl:       'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl:     'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

function RecenterControl({ lat, lng }) {
  const map = useMap();
  useEffect(() => { map.setView([lat, lng], map.getZoom()); }, [lat, lng]);
  return null;
}

/** Parse a GeoJSON polygon geometry string into [[lat, lng]] positions for Leaflet. */
function parsePolygonPositions(geometryStr) {
  if (!geometryStr) return null;
  try {
    const geo = typeof geometryStr === 'string' ? JSON.parse(geometryStr) : geometryStr;
    if (geo.type !== 'Polygon' || !geo.coordinates?.[0]) return null;
    // GeoJSON is [lng, lat]; Leaflet wants [lat, lng]
    return geo.coordinates[0].map(([lng, lat]) => [lat, lng]);
  } catch {
    return null;
  }
}

/**
 * Displays a property on an OpenStreetMap tile layer.
 * Shows a polygon boundary when `geometry` (GeoJSON string) is provided,
 * otherwise shows a pin at `lat`/`lng`.
 */
export default function PropertyMap({
  lat, lng, label = 'Property Location',
  geometry = null,
  height = '260px',
}) {
  const polygonPositions = parsePolygonPositions(geometry);
  const hasPolygon = polygonPositions && polygonPositions.length >= 3;

  // Need at least lat/lng OR a polygon to show anything
  if (!hasPolygon && (!lat || !lng)) return null;

  const centerLat = lat ?? polygonPositions[0][0];
  const centerLng = lng ?? polygonPositions[0][1];

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200" style={{ height }}>
      <MapContainer
        center={[centerLat, centerLng]}
        zoom={15}
        style={{ height: '100%', width: '100%' }}
        scrollWheelZoom={false}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        />

        <RecenterControl lat={centerLat} lng={centerLng} />

        {hasPolygon ? (
          <Polygon
            positions={polygonPositions}
            pathOptions={{ color: '#6366f1', fillColor: '#818cf8', fillOpacity: 0.2, weight: 2.5 }}
          />
        ) : (
          <Marker position={[lat, lng]}>
            <Popup>{label}</Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
}
