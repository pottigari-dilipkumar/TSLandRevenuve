import { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Polygon, Tooltip, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import {
  Search, X, MapPin, AlertTriangle, ChevronRight, User,
  FileText, GitBranch, Scale, Layers, Loader2,
} from 'lucide-react';
import { getParcels, searchByCoordinates, getLandHistory } from '../api/landMapApi';

// Fix Leaflet icon paths in Vite builds
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// ── Land type colours ─────────────────────────────────────────────────────────
export const LAND_TYPE_CONFIG = {
  PRIVATE:        { label: 'Private Land',        color: '#6366f1', fill: '#818cf8' },
  GOVERNMENT:     { label: 'Government Land',      color: '#dc2626', fill: '#f87171' },
  FOREST:         { label: 'Forest Reserve',       color: '#16a34a', fill: '#4ade80' },
  ASSIGNED:       { label: 'Assigned Land',        color: '#d97706', fill: '#fbbf24' },
  INAM:           { label: 'Inam / Grant',         color: '#7c3aed', fill: '#a78bfa' },
  WAQF:           { label: 'Waqf / Trust',         color: '#0891b2', fill: '#22d3ee' },
  NALA_CONVERTED: { label: 'NALA Converted',       color: '#ea580c', fill: '#fb923c' },
  LAKE:           { label: 'Lake / Water Body',    color: '#0369a1', fill: '#38bdf8' },
};

const DEFAULT_CONFIG = LAND_TYPE_CONFIG.PRIVATE;
const HYDERABAD = [17.385, 78.487];

// ── Helpers ───────────────────────────────────────────────────────────────────
function parsePositions(geometry) {
  if (!geometry || geometry.type !== 'Polygon') return null;
  try {
    return geometry.coordinates[0].map(([lng, lat]) => [lat, lng]);
  } catch {
    return null;
  }
}

function fmt(v) {
  if (v == null) return '—';
  if (typeof v === 'string') return v;
  return v;
}

function fmtDate(v) {
  if (!v) return '—';
  try { return new Date(v).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }); }
  catch { return v; }
}

function fmtAmount(v) {
  if (v == null) return '—';
  return '₹' + Number(v).toLocaleString('en-IN');
}

// ── Child components ──────────────────────────────────────────────────────────

function FlyTo({ target }) {
  const map = useMap();
  useEffect(() => { if (target) map.flyTo(target, 18, { duration: 1.2 }); }, [target, map]);
  return null;
}

function LandParcel({ feature, isSelected, onSelect }) {
  const [hovered, setHovered] = useState(false);
  const p = feature.properties;
  const positions = parsePositions(feature.geometry);
  if (!positions) return null;

  const cfg = LAND_TYPE_CONFIG[p.landType] || DEFAULT_CONFIG;

  const pathOptions = {
    color: isSelected ? '#fff' : cfg.color,
    fillColor: cfg.fill,
    weight: isSelected ? 4 : hovered ? 3 : p.hasActiveCases ? 2.5 : 1.5,
    fillOpacity: isSelected ? 0.65 : hovered ? 0.5 : p.hasActiveCases ? 0.45 : 0.28,
    dashArray: p.hasActiveCases && !isSelected ? '7 4' : null,
    opacity: 1,
  };

  return (
    <Polygon
      positions={positions}
      pathOptions={pathOptions}
      eventHandlers={{
        mouseover: () => setHovered(true),
        mouseout:  () => setHovered(false),
        click:     () => onSelect(feature),
      }}
    >
      <Tooltip sticky direction="top" offset={[0, -8]} className="land-tooltip">
        <div style={{ fontFamily: 'Inter,system-ui,sans-serif', minWidth: 190, padding: '2px 0' }}>
          <div style={{ fontWeight: 700, fontSize: 13, color: '#0f172a', marginBottom: 5 }}>
            Survey: {p.surveyNumber}
          </div>
          <div style={{ fontSize: 12, color: '#475569', marginBottom: 2 }}>👤 {p.ownerName}</div>
          <div style={{ fontSize: 12, color: '#475569', marginBottom: 2 }}>
            📍 {p.district}, {p.village}
          </div>
          <div style={{ fontSize: 12, color: '#475569', marginBottom: 2 }}>📐 {p.areaInAcres} acres</div>
          <div style={{
            display: 'inline-block', fontSize: 10, fontWeight: 600,
            padding: '2px 7px', borderRadius: 20, marginTop: 3,
            background: cfg.fill + '55', color: cfg.color, border: `1px solid ${cfg.color}44`,
          }}>
            {cfg.label}
          </div>
          {p.hasActiveCases && (
            <div style={{ fontSize: 11, color: '#dc2626', fontWeight: 600, marginTop: 5 }}>
              ⚠ {p.activeCaseCount} active legal case{p.activeCaseCount > 1 ? 's' : ''}
            </div>
          )}
          <div style={{ fontSize: 10, color: '#94a3b8', marginTop: 5 }}>Click for full history →</div>
        </div>
      </Tooltip>
    </Polygon>
  );
}

// ── Status badge ──────────────────────────────────────────────────────────────
function StatusBadge({ status }) {
  const map = {
    APPROVED:    { bg: '#dcfce7', text: '#16a34a', label: 'Approved' },
    DRAFT:       { bg: '#f1f5f9', text: '#64748b', label: 'Draft' },
    PENDING_APPROVAL: { bg: '#fef9c3', text: '#ca8a04', label: 'Pending' },
    REJECTED:    { bg: '#fee2e2', text: '#dc2626', label: 'Rejected' },
    PENDING:     { bg: '#fef9c3', text: '#ca8a04', label: 'Pending' },
    STAYED:      { bg: '#fce7f3', text: '#be185d', label: 'Stayed' },
    DISPOSED:    { bg: '#dcfce7', text: '#16a34a', label: 'Disposed' },
    APPLIED:     { bg: '#ede9fe', text: '#7c3aed', label: 'Applied' },
    APPROVED_MUTATION: { bg: '#dcfce7', text: '#16a34a', label: 'Approved' },
    REJECTED_MUTATION: { bg: '#fee2e2', text: '#dc2626', label: 'Rejected' },
  };
  const s = map[status] || { bg: '#f1f5f9', text: '#64748b', label: status };
  return (
    <span style={{
      background: s.bg, color: s.text, fontSize: 10, fontWeight: 700,
      padding: '2px 8px', borderRadius: 20, letterSpacing: '0.03em',
    }}>
      {s.label}
    </span>
  );
}

// ── Detail panel sections ─────────────────────────────────────────────────────
function Section({ icon: Icon, title, count, children }) {
  return (
    <div className="border-t border-slate-100 pt-4 mt-4">
      <div className="flex items-center gap-2 mb-3">
        <Icon size={14} className="text-brand-500 flex-shrink-0" />
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</span>
        {count != null && (
          <span className="ml-auto text-xs font-semibold bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">
            {count}
          </span>
        )}
      </div>
      {children}
    </div>
  );
}

function EmptyState({ message }) {
  return <p className="text-xs text-slate-400 py-2">{message}</p>;
}

// ── Main component ────────────────────────────────────────────────────────────
export default function LandMapPage() {
  const [parcels, setParcels]           = useState(null);
  const [loadingMap, setLoadingMap]     = useState(true);
  const [mapError, setMapError]         = useState(null);

  const [selectedId, setSelectedId]           = useState(null);
  const [history, setHistory]                 = useState(null);
  const [historyLoading, setHistoryLoading]   = useState(false);

  const [searchLat, setSearchLat]   = useState('');
  const [searchLng, setSearchLng]   = useState('');
  const [searchMsg, setSearchMsg]   = useState('');
  const [flyTarget, setFlyTarget]   = useState(null);

  const [visibleTypes, setVisibleTypes] = useState(() => new Set(Object.keys(LAND_TYPE_CONFIG)));
  const [showLegend, setShowLegend]     = useState(true);

  // Load all parcels on mount
  useEffect(() => {
    getParcels()
      .then(data => { setParcels(data); setLoadingMap(false); })
      .catch(() => { setMapError('Failed to load map data.'); setLoadingMap(false); });
  }, []);

  const selectParcel = async (feature) => {
    const id = feature.properties.id;
    setSelectedId(id);
    setHistory(null);
    setHistoryLoading(true);
    try {
      const h = await getLandHistory(id);
      setHistory(h);
    } catch {
      setHistory({ error: true });
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleCoordSearch = async (e) => {
    e.preventDefault();
    const lat = parseFloat(searchLat);
    const lng = parseFloat(searchLng);
    if (isNaN(lat) || isNaN(lng)) { setSearchMsg('Enter valid coordinates.'); return; }

    setFlyTarget([lat, lng]);
    setSearchMsg('');

    try {
      const res = await searchByCoordinates(lat, lng);
      if (res.found && res.landId && res.landId !== -1) {
        const feat = parcels?.features?.find(f => f.properties.id === res.landId);
        if (feat) selectParcel(feat);
        setSearchMsg('');
      } else {
        setSearchMsg('No land parcel found at these coordinates.');
        setSelectedId(null);
        setHistory(null);
      }
    } catch {
      setSearchMsg('Search failed. Try again.');
    }
  };

  const closePanel = () => { setSelectedId(null); setHistory(null); };

  const toggleType = (type) => {
    setVisibleTypes(prev => {
      const next = new Set(prev);
      next.has(type) ? next.delete(type) : next.add(type);
      return next;
    });
  };

  const visibleFeatures = parcels?.features?.filter(f => visibleTypes.has(f.properties.landType)) ?? [];

  return (
    <div
      className="-m-5 lg:-m-8 flex overflow-hidden bg-slate-100"
      style={{ height: 'calc(100vh - 57px)' }}
    >
      {/* ── Map area ────────────────────────────────────────────────────────── */}
      <div className="flex-1 relative">
        {/* Search bar */}
        <div className="absolute top-4 left-4 z-[1000] w-72">
          <form onSubmit={handleCoordSearch} className="card p-3 space-y-2 shadow-card-hover">
            <div className="flex items-center gap-2 mb-1">
              <MapPin size={14} className="text-brand-500" />
              <span className="text-xs font-semibold text-slate-700">Search by Coordinates</span>
            </div>
            <div className="flex gap-2">
              <input
                value={searchLat}
                onChange={e => setSearchLat(e.target.value)}
                placeholder="Latitude (17.38)"
                className="input text-xs py-1.5 flex-1"
              />
              <input
                value={searchLng}
                onChange={e => setSearchLng(e.target.value)}
                placeholder="Longitude (78.49)"
                className="input text-xs py-1.5 flex-1"
              />
            </div>
            <button type="submit" className="btn-primary w-full text-xs py-2 gap-1.5">
              <Search size={12} /> Search Location
            </button>
            {searchMsg && (
              <p className="text-xs text-amber-600 flex items-center gap-1">
                <AlertTriangle size={11} /> {searchMsg}
              </p>
            )}
          </form>
        </div>

        {/* Layer toggle button */}
        <button
          onClick={() => setShowLegend(v => !v)}
          className="absolute top-4 right-4 z-[1000] card p-2.5 shadow-card hover:shadow-card-hover transition-all"
          title="Toggle legend & layers"
        >
          <Layers size={16} className="text-slate-600" />
        </button>

        {/* Loading / error overlay */}
        {loadingMap && (
          <div className="absolute inset-0 z-[999] flex items-center justify-center bg-slate-100/80 backdrop-blur-sm">
            <div className="flex flex-col items-center gap-3">
              <Loader2 size={32} className="text-brand-500 animate-spin" />
              <p className="text-sm font-medium text-slate-600">Loading land parcel data…</p>
            </div>
          </div>
        )}
        {mapError && (
          <div className="absolute inset-0 z-[999] flex items-center justify-center">
            <div className="card p-6 text-center max-w-xs">
              <AlertTriangle size={32} className="text-amber-500 mx-auto mb-2" />
              <p className="text-sm text-slate-600">{mapError}</p>
            </div>
          </div>
        )}

        {/* Leaflet map */}
        <MapContainer
          center={HYDERABAD}
          zoom={12}
          style={{ height: '100%', width: '100%' }}
          scrollWheelZoom
          zoomControl={false}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          <FlyTo target={flyTarget} />
          {visibleFeatures.map(feature => (
            <LandParcel
              key={feature.properties.id}
              feature={feature}
              isSelected={feature.properties.id === selectedId}
              onSelect={selectParcel}
            />
          ))}
        </MapContainer>

        {/* Legend + layer toggles */}
        {showLegend && (
          <div className="absolute bottom-6 left-4 z-[1000] card p-3 shadow-card-hover max-w-[220px]">
            <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-400 mb-2.5">
              Land Types
            </p>
            <div className="space-y-1.5">
              {Object.entries(LAND_TYPE_CONFIG).map(([type, cfg]) => (
                <button
                  key={type}
                  onClick={() => toggleType(type)}
                  className={`flex items-center gap-2 w-full rounded-lg px-2 py-1 text-left transition-opacity ${
                    visibleTypes.has(type) ? 'opacity-100' : 'opacity-35'
                  }`}
                >
                  <span
                    className="w-3 h-3 rounded-sm flex-shrink-0 border"
                    style={{ background: cfg.fill, borderColor: cfg.color }}
                  />
                  <span className="text-[11px] text-slate-600 font-medium">{cfg.label}</span>
                </button>
              ))}
              <div className="mt-2 pt-2 border-t border-slate-100">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded-sm flex-shrink-0 bg-red-100 border border-dashed border-red-500" />
                  <span className="text-[11px] text-red-600 font-medium">Active Legal Dispute</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Parcel count badge */}
        {!loadingMap && parcels && (
          <div className="absolute bottom-6 right-4 z-[1000] card px-3 py-2 shadow-sm">
            <span className="text-xs font-semibold text-slate-600">
              {visibleFeatures.length} / {parcels.total ?? parcels.features?.length ?? 0} parcels
            </span>
          </div>
        )}
      </div>

      {/* ── Detail panel ────────────────────────────────────────────────────── */}
      {selectedId != null && (
        <div className="w-[400px] flex-shrink-0 bg-white border-l border-slate-200 flex flex-col overflow-hidden animate-fade-in">
          {/* Panel header */}
          <div className="bg-dark-gradient px-4 py-3 flex items-center gap-3 flex-shrink-0">
            <div className="w-8 h-8 rounded-xl bg-brand-gradient flex items-center justify-center flex-shrink-0">
              <MapPin size={14} className="text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white leading-none truncate">
                {history?.land?.surveyNumber ?? 'Loading…'}
              </p>
              <p className="text-[10px] text-slate-400 mt-0.5 truncate">
                {history?.land ? `${history.land.district} · ${history.land.village}` : ''}
              </p>
            </div>
            <button onClick={closePanel} className="text-slate-400 hover:text-white transition-colors flex-shrink-0">
              <X size={16} />
            </button>
          </div>

          {/* Panel body */}
          <div className="flex-1 overflow-y-auto p-4">
            {historyLoading && (
              <div className="flex flex-col items-center justify-center py-12 gap-3">
                <Loader2 size={24} className="text-brand-500 animate-spin" />
                <p className="text-xs text-slate-400">Loading land history…</p>
              </div>
            )}

            {!historyLoading && history?.error && (
              <div className="alert-error mt-2">
                <AlertTriangle size={14} /> Failed to load history.
              </div>
            )}

            {!historyLoading && history && !history.error && (
              <>
                {/* Land summary */}
                {(() => {
                  const l = history.land;
                  const cfg = LAND_TYPE_CONFIG[l.landType] || DEFAULT_CONFIG;
                  return (
                    <div className="rounded-xl border border-slate-100 bg-slate-50 p-3 space-y-2">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <p className="text-base font-bold text-slate-900">{l.surveyNumber}</p>
                          <p className="text-xs text-slate-500">{l.district} — {l.village}</p>
                        </div>
                        <span style={{
                          background: cfg.fill + '55', color: cfg.color,
                          border: `1px solid ${cfg.color}66`,
                          fontSize: 10, fontWeight: 700, padding: '3px 10px', borderRadius: 20,
                          whiteSpace: 'nowrap',
                        }}>
                          {cfg.label}
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                        <span className="text-slate-400">Area</span>
                        <span className="font-semibold text-slate-700">{l.areaInAcres} acres</span>
                        {l.plusCode && (
                          <>
                            <span className="text-slate-400">PLUS Code</span>
                            <code className="font-mono text-brand-600 text-[11px]">{l.plusCode}</code>
                          </>
                        )}
                        {l.prohibited && (
                          <span className="col-span-2 text-red-600 font-semibold flex items-center gap-1 mt-1">
                            <AlertTriangle size={11} /> Transfer Prohibited
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })()}

                {/* Owner */}
                {history.owner && (
                  <Section icon={User} title="Current Owner">
                    <div className="rounded-xl bg-slate-50 border border-slate-100 p-3 space-y-1.5">
                      <p className="text-sm font-semibold text-slate-800">{history.owner.name}</p>
                      <p className="text-xs text-slate-500">
                        Aadhaar: <span className="font-mono">{history.owner.nationalId}</span>
                      </p>
                    </div>
                  </Section>
                )}

                {/* Registration history */}
                <Section
                  icon={FileText}
                  title="Registration History"
                  count={history.registrations?.length}
                >
                  {(!history.registrations || history.registrations.length === 0)
                    ? <EmptyState message="No registrations recorded for this parcel." />
                    : history.registrations.map((r, i) => (
                      <div key={i} className="rounded-xl border border-slate-100 p-3 mb-2 last:mb-0 bg-white space-y-1.5 text-xs">
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-bold text-slate-800">{r.ref}</span>
                          <StatusBadge status={r.status} />
                        </div>
                        <div className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1">
                          <span className="text-slate-400">Buyer</span>
                          <span className="font-medium text-slate-700">{fmt(r.buyerName)} <span className="text-slate-400 font-normal">({r.buyerAadhaar})</span></span>
                          <span className="text-slate-400">Seller</span>
                          <span className="font-medium text-slate-700">{fmt(r.sellerName)} <span className="text-slate-400 font-normal">({r.sellerAadhaar})</span></span>
                          {r.considerationAmount && (
                            <>
                              <span className="text-slate-400">Sale Value</span>
                              <span className="font-semibold text-emerald-700">{fmtAmount(r.considerationAmount)}</span>
                            </>
                          )}
                          {r.stampDuty && (
                            <>
                              <span className="text-slate-400">Stamp Duty</span>
                              <span className="text-slate-700">{fmtAmount(r.stampDuty)}</span>
                            </>
                          )}
                          {r.approvedByUserId && (
                            <>
                              <span className="text-slate-400">SRO (User ID)</span>
                              <span className="text-slate-700">#{r.approvedByUserId}</span>
                            </>
                          )}
                          <span className="text-slate-400">Filed</span>
                          <span className="text-slate-600">{fmtDate(r.createdAt)}</span>
                          {r.decidedAt && (
                            <>
                              <span className="text-slate-400">Decided</span>
                              <span className="text-slate-600">{fmtDate(r.decidedAt)}</span>
                            </>
                          )}
                        </div>
                        {r.blockchainTxHash && (
                          <div className="mt-1 rounded-lg bg-indigo-50 border border-indigo-100 px-2.5 py-1.5">
                            <p className="text-[10px] text-indigo-500 font-semibold mb-0.5">Blockchain Anchor</p>
                            <code className="text-[10px] font-mono text-indigo-700 break-all">{r.blockchainTxHash}</code>
                            {r.blockchainBlockNumber && (
                              <p className="text-[10px] text-indigo-400">Block #{r.blockchainBlockNumber}</p>
                            )}
                          </div>
                        )}
                        {r.rejectionReason && (
                          <p className="text-[11px] text-red-600 bg-red-50 rounded-lg px-2.5 py-1.5">
                            ✕ {r.rejectionReason}
                          </p>
                        )}
                      </div>
                    ))}
                </Section>

                {/* Mutation history */}
                <Section
                  icon={GitBranch}
                  title="Ownership Mutations"
                  count={history.mutations?.length}
                >
                  {(!history.mutations || history.mutations.length === 0)
                    ? <EmptyState message="No mutation records found." />
                    : history.mutations.map((m, i) => (
                      <div key={i} className="rounded-xl border border-slate-100 p-3 mb-2 last:mb-0 bg-white text-xs space-y-1.5">
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-bold text-slate-800">{m.ref}</span>
                          <StatusBadge status={m.status} />
                        </div>
                        <div className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1">
                          <span className="text-slate-400">Type</span>
                          <span className="font-medium text-slate-700">{(m.type || '').replace(/_/g, ' ')}</span>
                          <span className="text-slate-400">From</span>
                          <span className="text-slate-700">{fmt(m.previousOwner)}</span>
                          <span className="text-slate-400">To</span>
                          <span className="font-semibold text-slate-800">{fmt(m.newOwner)}</span>
                          <span className="text-slate-400">Applied</span>
                          <span className="text-slate-600">{fmtDate(m.appliedAt)}</span>
                          {m.decidedAt && (
                            <>
                              <span className="text-slate-400">Decided</span>
                              <span className="text-slate-600">{fmtDate(m.decidedAt)}</span>
                            </>
                          )}
                        </div>
                        {m.registrationRef && (
                          <p className="text-[11px] text-indigo-600">
                            <ChevronRight size={10} className="inline" /> Linked to registration {m.registrationRef}
                          </p>
                        )}
                      </div>
                    ))}
                </Section>

                {/* Legal cases */}
                <Section
                  icon={Scale}
                  title="Legal Cases / Disputes"
                  count={history.legalCases?.length}
                >
                  {(!history.legalCases || history.legalCases.length === 0)
                    ? <EmptyState message="No legal cases recorded." />
                    : history.legalCases.map((c, i) => (
                      <div key={i} className={`rounded-xl border p-3 mb-2 last:mb-0 text-xs space-y-1 ${
                        c.status === 'PENDING' || c.status === 'STAYED'
                          ? 'border-red-200 bg-red-50'
                          : 'border-slate-100 bg-white'
                      }`}>
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-bold text-slate-800">{c.caseNumber}</span>
                          <StatusBadge status={c.status} />
                        </div>
                        <div className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1">
                          {c.court && (
                            <>
                              <span className="text-slate-400">Court</span>
                              <span className="text-slate-700">{c.court}</span>
                            </>
                          )}
                          {c.caseType && (
                            <>
                              <span className="text-slate-400">Type</span>
                              <span className="font-semibold text-slate-700">{c.caseType}</span>
                            </>
                          )}
                          {c.filedBy && (
                            <>
                              <span className="text-slate-400">Filed By</span>
                              <span className="text-slate-700">{c.filedBy}</span>
                            </>
                          )}
                          {c.filedDate && (
                            <>
                              <span className="text-slate-400">Filed</span>
                              <span className="text-slate-600">{fmtDate(c.filedDate)}</span>
                            </>
                          )}
                        </div>
                        {c.description && (
                          <p className="text-[11px] text-slate-500 mt-1 italic">{c.description}</p>
                        )}
                      </div>
                    ))}
                </Section>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
