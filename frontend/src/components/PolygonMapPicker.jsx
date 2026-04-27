/**
 * PolygonMapPicker — click-to-draw land parcel boundary on an OpenStreetMap.
 *
 * Usage:
 *   <PolygonMapPicker value={geo} onChange={(geo) => setGeo(geo)} />
 *
 * `geo` shape: { geometry: string (GeoJSON), plusCode: string, centroid: {lat,lng} }
 * Pass null to clear.
 */

import { useState, useCallback, useEffect } from 'react';
import {
  MapContainer, TileLayer, Polygon, Polyline,
  CircleMarker, useMapEvents, useMap,
} from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapPin, Undo2, Trash2, CheckCircle2, Navigation } from 'lucide-react';

// Fix Leaflet icon paths in Vite builds
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// ── PLUS Code encoder ─────────────────────────────────────────────────────────
const OLC_ALPHABET = '23456789CFGHJMPQRVWX';

function encodePlusCode(lat, lng) {
  try {
    let normLat = lat + 90;
    let normLng = lng + 180;
    let code = '';
    let digit = 0;
    for (let i = 0; i < 5; i++) {
      const latDiv = Math.floor(normLat / Math.pow(20, 4 - i));
      const lngDiv = Math.floor(normLng / Math.pow(20, 4 - i));
      normLat -= latDiv * Math.pow(20, 4 - i);
      normLng -= lngDiv * Math.pow(20, 4 - i);
      code += OLC_ALPHABET[latDiv] + OLC_ALPHABET[lngDiv];
      digit += 2;
      if (digit === 8) code += '+';
    }
    return code.substring(0, 11);
  } catch {
    return '';
  }
}

function computeCentroid(points) {
  const lat = points.reduce((s, p) => s + p[0], 0) / points.length;
  const lng = points.reduce((s, p) => s + p[1], 0) / points.length;
  return { lat, lng };
}

function buildGeoJson(points) {
  const ring = [...points, points[0]].map(([lat, lng]) => [lng, lat]);
  return { type: 'Polygon', coordinates: [ring] };
}

// ── Map click handler — returns null so react-leaflet treats it as a layer ───

function ClickCapture({ onAdd, active }) {
  useMapEvents({
    click: (e) => {
      if (active) onAdd([e.latlng.lat, e.latlng.lng]);
    },
  });
  return null;
}

// ── GPS fly-to — must live inside MapContainer to call useMap() ───────────────

function FlyToTarget({ target }) {
  const map = useMap();
  useEffect(() => {
    if (target) map.flyTo(target, 17);
  }, [target, map]);
  return null;
}

// ── Main component ────────────────────────────────────────────────────────────

const DEFAULT_CENTER = [17.385, 78.487]; // Hyderabad

export default function PolygonMapPicker({ value, onChange, height = '380px' }) {
  const [gpsTarget, setGpsTarget] = useState(null);

  // Parse existing geometry back to points when editing a saved registration
  const parseExisting = () => {
    if (!value?.geometry) return { pts: [], closed: false };
    try {
      const geo = typeof value.geometry === 'string'
        ? JSON.parse(value.geometry)
        : value.geometry;
      const ring = geo.coordinates[0];
      const pts = ring.slice(0, -1).map(([lng, lat]) => [lat, lng]);
      return { pts, closed: true };
    } catch {
      return { pts: [], closed: false };
    }
  };

  const existing = parseExisting();
  const [points, setPoints] = useState(existing.pts);
  const [closed, setClosed] = useState(existing.closed);

  const addPoint = useCallback((latlng) => {
    if (closed) return;
    setPoints((prev) => [...prev, latlng]);
  }, [closed]);

  const undo = () => {
    if (closed) { setClosed(false); return; }
    setPoints((prev) => prev.slice(0, -1));
  };

  const clear = () => {
    setPoints([]);
    setClosed(false);
    onChange(null);
  };

  const closePolygon = () => {
    if (points.length < 3) return;
    const centroid = computeCentroid(points);
    const geometry = buildGeoJson(points);
    const plusCode = encodePlusCode(centroid.lat, centroid.lng);
    setClosed(true);
    onChange({ geometry: JSON.stringify(geometry), plusCode, centroid });
  };

  const goToGps = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      const latlng = [pos.coords.latitude, pos.coords.longitude];
      setGpsTarget(latlng);
      if (!closed) addPoint(latlng);
    });
  };

  return (
    <div className="space-y-3">
      {/* Instruction banner */}
      <div className="flex items-start gap-2 rounded-lg bg-indigo-50 border border-indigo-100 px-3 py-2 text-xs text-indigo-700">
        <MapPin size={14} className="mt-0.5 flex-shrink-0" />
        <span>
          {closed
            ? 'Boundary closed. Undo to re-open and edit, or Clear to start over.'
            : points.length === 0
              ? 'Click on the map to mark the land boundary corners one by one.'
              : points.length < 3
                ? `${points.length} point(s) — need at least 3 to close the boundary.`
                : `${points.length} points added. Click "Close Boundary" when done.`}
        </span>
      </div>

      {/* Map */}
      <div className="overflow-hidden rounded-xl border border-slate-200" style={{ height }}>
        <MapContainer
          center={DEFAULT_CENTER}
          zoom={13}
          style={{ height: '100%', width: '100%' }}
          scrollWheelZoom
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          />

          {/* GPS fly-to — safe child using useMap() */}
          <FlyToTarget target={gpsTarget} />

          {/* Map click capture — returns null, safe inside MapContainer */}
          <ClickCapture onAdd={addPoint} active={!closed} />

          {/* Corner point markers */}
          {points.map((p, i) => (
            <CircleMarker
              key={i}
              center={p}
              radius={6}
              pathOptions={{
                color: '#6366f1',
                fillColor: '#818cf8',
                fillOpacity: 1,
                weight: 2,
              }}
            />
          ))}

          {/* In-progress outline while drawing */}
          {!closed && points.length >= 2 && (
            <Polyline
              positions={points}
              pathOptions={{ color: '#6366f1', weight: 2, dashArray: '6 4' }}
            />
          )}

          {/* Filled polygon after closing */}
          {closed && points.length >= 3 && (
            <Polygon
              positions={points}
              pathOptions={{
                color: '#6366f1',
                fillColor: '#818cf8',
                fillOpacity: 0.25,
                weight: 2.5,
              }}
            />
          )}
        </MapContainer>
      </div>

      {/* Controls — rendered outside MapContainer, no react-leaflet context issues */}
      <div className="flex flex-wrap items-center gap-2">
        <button
          type="button"
          title="Use my GPS location as the next point"
          onClick={goToGps}
          className="btn-secondary flex items-center gap-1.5 text-xs"
        >
          <Navigation size={13} /> My Location
        </button>

        <button
          type="button"
          className="btn-ghost flex items-center gap-1.5 text-xs"
          onClick={undo}
          disabled={points.length === 0 && !closed}
        >
          <Undo2 size={13} /> Undo
        </button>

        <button
          type="button"
          className="btn-ghost flex items-center gap-1.5 text-xs text-red-600"
          onClick={clear}
          disabled={points.length === 0 && !closed}
        >
          <Trash2 size={13} /> Clear
        </button>

        {!closed && (
          <button
            type="button"
            className="btn-primary flex items-center gap-1.5 text-xs"
            onClick={closePolygon}
            disabled={points.length < 3}
          >
            <CheckCircle2 size={13} /> Close Boundary
          </button>
        )}

        {/* PLUS Code badge */}
        {closed && value?.plusCode && (
          <div className="ml-auto flex items-center gap-2 rounded-lg bg-emerald-50 border border-emerald-100 px-3 py-1.5 text-xs">
            <span className="text-emerald-600 font-medium">PLUS Code</span>
            <code className="rounded bg-emerald-100 px-1.5 py-0.5 font-mono font-bold text-emerald-800 tracking-wider">
              {value.plusCode}
            </code>
          </div>
        )}
      </div>

      {/* Centroid coordinates */}
      {closed && value?.centroid && (
        <p className="text-xs text-slate-400">
          Centroid: {value.centroid.lat.toFixed(6)}°N, {value.centroid.lng.toFixed(6)}°E
          &nbsp;·&nbsp;{points.length} boundary points
        </p>
      )}
    </div>
  );
}
