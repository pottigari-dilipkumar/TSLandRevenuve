package in.gov.landrevenue.clean.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_witnesses")
public class RegistrationWitness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private LandRegistration landRegistration;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String aadhaarNumber;

    private String mobile;

    @Column(length = 500)
    private String address;

    private int sequenceNumber;

    public Long getId() { return id; }
    public LandRegistration getLandRegistration() { return landRegistration; }
    public void setLandRegistration(LandRegistration landRegistration) { this.landRegistration = landRegistration; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}
