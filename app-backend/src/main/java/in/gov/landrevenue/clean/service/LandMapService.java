/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.gov.landrevenue.clean.entity.LandRecord;
import in.gov.landrevenue.clean.entity.LandRegistration;
import in.gov.landrevenue.clean.entity.LegalCase;
import in.gov.landrevenue.clean.entity.MutationApplication;
import in.gov.landrevenue.clean.repository.LandRecordRepository;
import in.gov.landrevenue.clean.repository.LandRegistrationRepository;
import in.gov.landrevenue.clean.repository.LegalCaseRepository;
import in.gov.landrevenue.clean.repository.MutationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LandMapService {

    private static final Logger log = LoggerFactory.getLogger(LandMapService.class);
    private static final List<String> ACTIVE_CASE_STATUSES = List.of("PENDING", "STAYED");

    private final LandRecordRepository landRecordRepository;
    private final LandRegistrationRepository registrationRepository;
    private final MutationRepository mutationRepository;
    private final LegalCaseRepository legalCaseRepository;
    private final ObjectMapper objectMapper;

    public LandMapService(LandRecordRepository landRecordRepository,
                          LandRegistrationRepository registrationRepository,
                          MutationRepository mutationRepository,
                          LegalCaseRepository legalCaseRepository,
                          ObjectMapper objectMapper) {
        this.landRecordRepository = landRecordRepository;
        this.registrationRepository = registrationRepository;
        this.mutationRepository = mutationRepository;
        this.legalCaseRepository = legalCaseRepository;
        this.objectMapper = objectMapper;
    }

    /** Returns a GeoJSON FeatureCollection of all land parcels that have geometry. */
    public Map<String, Object> getFeatureCollection() {
        List<LandRecord> lands = landRecordRepository.findAll();
        List<Map<String, Object>> features = lands.stream()
                .filter(lr -> lr.getGeometry() != null)
                .map(this::buildFeature)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> fc = new LinkedHashMap<>();
        fc.put("type", "FeatureCollection");
        fc.put("features", features);
        fc.put("total", features.size());
        return fc;
    }

    /** Point-in-polygon search: returns the first land record ID whose polygon contains (lat, lng). */
    public Optional<Long> findByCoordinates(double lat, double lng) {
        return landRecordRepository.findAll().stream()
                .filter(lr -> lr.getGeometry() != null)
                .filter(lr -> {
                    double[][] ring = extractRing(lr.getGeometry());
                    return ring != null && pointInPolygon(lat, lng, ring);
                })
                .findFirst()
                .map(LandRecord::getId);
    }

    /** Full history for a single land parcel: land info, owner, registrations, mutations, legal cases. */
    public Map<String, Object> getLandHistory(Long id) {
        LandRecord lr = landRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Land record not found: " + id));

        List<LandRegistration> registrations = registrationRepository.findByProperty(
                lr.getDistrict(), lr.getVillage(), lr.getSurveyNumber());

        List<MutationApplication> mutations =
                mutationRepository.findByLandRecordIdOrderByAppliedAtDesc(lr.getId());

        List<LegalCase> legalCases =
                legalCaseRepository.findByLandRecordIdOrderByFiledDateDesc(lr.getId());

        Map<String, Object> response = new LinkedHashMap<>();

        // Land summary
        Map<String, Object> land = new LinkedHashMap<>();
        land.put("id", lr.getId());
        land.put("surveyNumber", lr.getSurveyNumber());
        land.put("district", lr.getDistrict());
        land.put("village", lr.getVillage());
        land.put("areaInAcres", lr.getAreaInAcres());
        land.put("landType", lr.getLandType().name());
        land.put("prohibited", lr.isProhibited());
        land.put("plusCode", lr.getPlusCode());
        land.put("geometry", lr.getGeometry());
        response.put("land", land);

        // Owner
        if (lr.getOwner() != null) {
            Map<String, Object> owner = new LinkedHashMap<>();
            owner.put("id", lr.getOwner().getId());
            owner.put("name", lr.getOwner().getName());
            owner.put("nationalId", maskId(lr.getOwner().getNationalId()));
            response.put("owner", owner);
        }

        // Registration history
        response.put("registrations", registrations.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ref", r.getRegistrationRef());
            m.put("status", r.getStatus().name());
            m.put("buyerName", r.getBuyerName());
            m.put("buyerAadhaar", maskId(r.getBuyerAadhaar()));
            m.put("sellerName", r.getSellerName());
            m.put("sellerAadhaar", maskId(r.getSellerAadhaar()));
            m.put("considerationAmount", r.getConsiderationAmount());
            m.put("totalMarketValue", r.getTotalMarketValue());
            m.put("stampDuty", r.getStampDuty());
            m.put("approvedByUserId", r.getApprovedByUserId());
            m.put("createdAt", r.getCreatedAt());
            m.put("submittedAt", r.getSubmittedAt());
            m.put("decidedAt", r.getDecidedAt());
            m.put("blockchainTxHash", r.getBlockchainTxHash());
            m.put("blockchainBlockNumber", r.getBlockchainBlockNumber());
            m.put("rejectionReason", r.getRejectionReason());
            return m;
        }).collect(Collectors.toList()));

        // Mutation history
        response.put("mutations", mutations.stream().map(m -> {
            Map<String, Object> mp = new LinkedHashMap<>();
            mp.put("ref", m.getMutationRef());
            mp.put("type", m.getMutationType());
            mp.put("status", m.getStatus().name());
            mp.put("previousOwner", m.getPreviousOwnerName());
            mp.put("newOwner", m.getNewOwnerName());
            mp.put("registrationRef", m.getRegistrationRef());
            mp.put("appliedAt", m.getAppliedAt());
            mp.put("decidedAt", m.getDecidedAt());
            return mp;
        }).collect(Collectors.toList()));

        // Legal cases
        response.put("legalCases", legalCases.stream().map(c -> {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("id", c.getId());
            cm.put("caseNumber", c.getCaseNumber());
            cm.put("court", c.getCourt());
            cm.put("caseType", c.getCaseType());
            cm.put("status", c.getStatus());
            cm.put("filedBy", c.getFiledBy());
            cm.put("filedDate", c.getFiledDate());
            cm.put("description", c.getDescription());
            return cm;
        }).collect(Collectors.toList()));

        return response;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildFeature(LandRecord lr) {
        Map<String, Object> geometry = parseGeometry(lr.getGeometry());
        if (geometry == null) return null;

        long activeCases = legalCaseRepository.countByLandRecordIdAndStatusIn(
                lr.getId(), ACTIVE_CASE_STATUSES);

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id", lr.getId());
        props.put("surveyNumber", lr.getSurveyNumber());
        props.put("district", lr.getDistrict());
        props.put("village", lr.getVillage());
        props.put("areaInAcres", lr.getAreaInAcres());
        props.put("landType", lr.getLandType().name());
        props.put("ownerName", lr.getOwner() != null ? lr.getOwner().getName() : "Unknown");
        props.put("ownerId", lr.getOwner() != null ? lr.getOwner().getId() : null);
        props.put("prohibited", lr.isProhibited());
        props.put("plusCode", lr.getPlusCode());
        props.put("hasActiveCases", activeCases > 0);
        props.put("activeCaseCount", activeCases);

        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("geometry", geometry);
        feature.put("properties", props);
        return feature;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGeometry(String geometryStr) {
        try {
            return objectMapper.readValue(geometryStr, Map.class);
        } catch (Exception e) {
            log.debug("Invalid geometry JSON: {}", e.getMessage());
            return null;
        }
    }

    /** Extracts the outer ring of a GeoJSON Polygon as [[lng, lat], ...] */
    private double[][] extractRing(String geometryStr) {
        try {
            JsonNode geo = objectMapper.readTree(geometryStr);
            JsonNode ring = geo.get("coordinates").get(0);
            double[][] result = new double[ring.size()][2];
            for (int i = 0; i < ring.size(); i++) {
                result[i][0] = ring.get(i).get(0).asDouble(); // lng
                result[i][1] = ring.get(i).get(1).asDouble(); // lat
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ray-casting point-in-polygon test.
     * ring: each row is [lng, lat] (GeoJSON order).
     */
    private boolean pointInPolygon(double pointLat, double pointLng, double[][] ring) {
        int n = ring.length;
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double lng_i = ring[i][0], lat_i = ring[i][1];
            double lng_j = ring[j][0], lat_j = ring[j][1];
            if (((lat_i > pointLat) != (lat_j > pointLat)) &&
                (pointLng < (lng_j - lng_i) * (pointLat - lat_i) / (lat_j - lat_i) + lng_i)) {
                inside = !inside;
            }
        }
        return inside;
    }

    private String maskId(String id) {
        if (id == null || id.length() < 4) return "****";
        return "XXXX-XX-" + id.substring(id.length() - 4);
    }
}
