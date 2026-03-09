export type Role = 'ADMIN' | 'SRO' | 'USER';

export interface GeoPoint { lat: number; lng: number; }

export interface LandParcel {
  landId: string;
  village: string;
  surveyNumber: string;
  seller: string;
  buyer: string;
  polygon: GeoPoint[];
  history: { at: string; actor: string; action: string; remarks: string }[];
  documents: { documentId: string; name: string }[];
}
