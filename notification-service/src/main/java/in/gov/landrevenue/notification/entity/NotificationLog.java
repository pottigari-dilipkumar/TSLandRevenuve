package in.gov.landrevenue.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notif_logs",
       indexes = {
           @Index(name = "idx_notif_channel", columnList = "channel"),
           @Index(name = "idx_notif_reference", columnList = "reference"),
           @Index(name = "idx_notif_created_at", columnList = "createdAt")
       })
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String channel;         // EMAIL, SMS, WHATSAPP, OTP

    @Column(nullable = false)
    private String recipient;

    @Column(length = 500)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 20)
    private String status;          // SENT, FAILED, MOCK

    @Column(length = 100)
    private String reference;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public NotificationLog() {}

    public NotificationLog(String channel, String recipient, String subject,
                           String message, String status, String reference) {
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.reference = reference;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getReference() { return reference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
