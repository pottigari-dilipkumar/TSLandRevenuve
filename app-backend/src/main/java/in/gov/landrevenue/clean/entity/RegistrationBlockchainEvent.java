package in.gov.landrevenue.clean.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Immutable audit trail entry for every registration state transition.
 * Each event includes a SHA-256 payload hash, making the chain tamper-evident.
 * When blockchain is enabled and the registration is APPROVED, the on-chain
 * transaction hash and block number are also stored.
 */
@Entity
@Table(name = "registration_blockchain_events",
       indexes = @Index(name = "idx_reg_bc_events_ref", columnList = "registrationRef"))
public class RegistrationBlockchainEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String registrationRef;

    /** DRAFTED | SUBMITTED | APPROVED | REJECTED | DOCUMENT_UPLOADED | BLOCKCHAIN_ANCHORED */
    @Column(nullable = false, length = 30)
    private String eventType;

    private String actorUsername;
    private String actorRole;

    /** SHA-256( eventType | registrationRef | epochMillis | actorUsername ) — tamper-evident fingerprint */
    @Column(length = 64)
    private String payloadHash;

    /** Only populated for BLOCKCHAIN_ANCHORED events */
    @Column(length = 100)
    private String txHash;

    private Long blockNumber;

    /** SYNCED | FAILED | SKIPPED */
    @Column(length = 10)
    private String chainSyncStatus;

    @Column(nullable = false)
    private Instant timestamp;

    /** Monotonically increasing within a registration */
    private Long sequence;

    @Column(columnDefinition = "TEXT")
    private String details;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getRegistrationRef() { return registrationRef; }
    public void setRegistrationRef(String registrationRef) { this.registrationRef = registrationRef; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public String getChainSyncStatus() { return chainSyncStatus; }
    public void setChainSyncStatus(String chainSyncStatus) { this.chainSyncStatus = chainSyncStatus; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getSequence() { return sequence; }
    public void setSequence(Long sequence) { this.sequence = sequence; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
