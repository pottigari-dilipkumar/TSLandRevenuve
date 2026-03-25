package in.gov.landrevenue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "registration_records")
public class RegistrationRecordEntity {

    @Id
    @Column(name = "registration_ref", nullable = false, updatable = false)
    private String registrationRef;

    @Column(name = "parcel_id", nullable = false)
    private String parcelId;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Column(name = "buyer_name", nullable = false)
    private String buyerName;

    @Column(name = "deed_hash", nullable = false)
    private String deedHash;

    @Column(name = "verified_identity_token", nullable = false)
    private String verifiedIdentityToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false)
    private String status;

    public String getRegistrationRef() {
        return registrationRef;
    }

    public void setRegistrationRef(String registrationRef) {
        this.registrationRef = registrationRef;
    }

    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getDeedHash() {
        return deedHash;
    }

    public void setDeedHash(String deedHash) {
        this.deedHash = deedHash;
    }

    public String getVerifiedIdentityToken() {
        return verifiedIdentityToken;
    }

    public void setVerifiedIdentityToken(String verifiedIdentityToken) {
        this.verifiedIdentityToken = verifiedIdentityToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
