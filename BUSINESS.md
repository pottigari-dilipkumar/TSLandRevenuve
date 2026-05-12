# Telangana Land Revenue & Registration System — Business Overview

> © 2026 DivaTech. All rights reserved. Proprietary & Confidential.

---

## The Problem We Solve

Land ownership in India is one of the most disputed and fraud-prone areas of governance. Every year, thousands of citizens face:

- **Fraudulent property sales** — the same land sold to multiple buyers
- **Missing ownership history** — no clear record of who owned land before the current owner
- **Manual, paper-based processes** — registrations done on paper are lost, tampered with, or misplaced
- **Opaque government processes** — citizens have no visibility into the status of their applications
- **Encroachment and boundary disputes** — no digital map of exactly where a parcel of land begins and ends

This system is built to address all of these problems for the state of Telangana.

---

## What the System Does

### 1. Land Records — The Single Source of Truth

Every piece of land in Telangana is assigned a **Survey Number** — a unique government identifier. This system maintains a central, tamper-evident registry of:

- Who owns each parcel of land (the **owner**)
- Where exactly the land is located (district, village, and a **drawn boundary on a map**)
- How large the parcel is (in acres)
- What type of land it is (private, government, forest, assigned, WAQF, etc.)
- A **PLUS Code** — the Government of India's modern location pin that precisely identifies the parcel on a map, similar to a postal PIN code but for any specific piece of land

---

### 2. Property Registration — Buying and Selling Land (Citizen-Driven Checker-Maker Workflow)

When a citizen wants to sell land they own, the registration goes through a **multi-party, multi-step approval chain** before the ownership officially transfers. This is called the **Checker-Maker workflow** and is fully citizen-driven — no officer intervention is required to initiate it.

#### Full Workflow — Step by Step

```
SELLER (Citizen)         BUYER (Citizen)       SRO ASSISTANT          SRO (Final Authority)
       │                       │                      │                        │
  1. Creates sale              │                      │                        │
     request (DRAFT)           │                      │                        │
       │                       │                      │                        │
  2. Reviews & submits ───────►│                      │                        │
     (AWAITING_BUYER           │                      │                        │
      _APPROVAL)               │                      │                        │
                          3. Reviews the              │                        │
                             sale details             │                        │
                             & consents               │                        │
                                  │ (PENDING_REVIEW)  │                        │
                                  └──────────────────►│                        │
                                                 4a. Reviews docs              │
                                                     & forwards ──────────────►│
                                                     (PENDING_APPROVAL)        │
                                                                          5. Final
                                                                             approve/reject
                                                                             (APPROVED /
                                                                              REJECTED)
                       ── OR ──
                                                 4b. Sends back for
                                                     revision
                                                     (REVISION_REQUIRED)
                                                          │
                                                  Seller reviews notes
                                                  & resubmits
                                                  (→ PENDING_REVIEW)
```

#### Registration Status Definitions

| Status | Meaning |
|---|---|
| `DRAFT` | Seller has created the sale request but not yet sent it to the buyer |
| `AWAITING_BUYER_APPROVAL` | Seller has submitted; buyer must review and consent |
| `BUYER_REJECTED` | Buyer declined the sale; seller is notified |
| `PENDING_REVIEW` | Both parties consented; under SRO Assistant review |
| `REVISION_REQUIRED` | SRO Assistant found issues; seller must correct and resubmit |
| `PENDING_APPROVAL` | SRO Assistant satisfied; waiting for SRO's final decision |
| `APPROVED` | SRO approved the registration; ownership transfers to buyer automatically |
| `REJECTED` | SRO rejected the registration; both parties are notified |

#### What Happens on Approval

When the SRO approves a registration:
1. The **buyer automatically becomes the new recorded owner** in the Land Records
2. The **land parcel boundary on the map** is updated to reflect the new ownership
3. The registration is optionally **anchored to the blockchain** — creating a tamper-proof, independently verifiable stamp of the ownership transfer
4. Both the **seller and buyer receive a notification** confirming the outcome

#### What Happens on Buyer Rejection or SRO Rejection

- **Buyer rejection** — the seller is notified immediately. The registration stays in `BUYER_REJECTED` state. The seller may create a new request if desired.
- **SRO rejection** — both seller and buyer are notified. The registration is permanently closed with a stated reason.

---

### 3. Staff-Initiated Registration

In addition to the citizen-driven flow, authorised staff (SRO, SRO Assistant, Admin) can also create registrations directly on behalf of parties — for example, when a citizen visits the registration office in person. These go directly from `DRAFT` to `PENDING_APPROVAL` without the buyer consent step, and are reviewed and approved by the SRO.

---

### 4. Mutation — Transferring Ownership for Other Reasons

Ownership can change for reasons other than a sale — inheritance, court orders, government assignment, etc. The **Mutation** workflow handles these cases:

- A mutation request is filed explaining why ownership should change
- It goes through a **Mandal-level review** before approval
- Once approved, the ownership in the land records is updated automatically

---

### 5. Ownership History — Full Transparency

Anyone authorised can see the **complete ownership history** of any land parcel:

- Every previous owner, in chronological order
- Whether the transfer happened through a registration (sale) or a mutation (inheritance, etc.)
- The status of each transaction (approved, rejected, pending)
- The exact date the ownership changed

This makes it nearly impossible to hide fraudulent transfers or disputes.

---

### 6. Revenue Collection

The system tracks **revenue payments** associated with land — taxes, fees, and dues owed to the government. Officers can record payments and view summaries of what has been collected and what is outstanding.

---

### 7. Public Property Search & Encumbrance Certificate

Any citizen (even without logging in) can:

- **Search for a land parcel** by district, village, and survey number
- **Generate an Encumbrance Certificate (EC)** — an official document showing all registered transactions on a property, used by banks when approving home loans

This removes the need for citizens to visit government offices for basic information.

---

### 8. Market Values

The system maintains **government-published market rate per acre** for every district and village combination. These rates are used to:

- Automatically calculate the **total market value** of a parcel being registered
- Compute the **stamp duty** (7% of total market value) owed to the government
- Provide a reference for citizens checking current rates in any area

---

### 9. Notification Bell — Real-Time Workflow Alerts

Every participant in a workflow is **automatically notified in-app** the moment something requires their attention or changes. Notifications are targeted per user based on their role and Aadhaar identity.

| Event | Who Gets Notified |
|---|---|
| Seller submits sale request | Buyer — "You have a sale request awaiting your consent" |
| Buyer consents | Seller — "Buyer approved"; all SRO Assistants — "New case to review" |
| Buyer rejects | Seller — "Buyer rejected the sale" |
| SRO Assistant sends back for revision | Seller — "Revision required" with notes |
| SRO Assistant forwards to SRO | All SROs — "Registration pending final approval" |
| Seller resubmits after revision | All SRO Assistants — "Case resubmitted" |
| SRO approves | Seller — "Sale approved"; Buyer — "Land ownership transferred to you" |
| SRO rejects | Both seller and buyer — "Registration rejected" with reason |

The bell icon in the top navigation bar shows a live **unread count badge**. Clicking it opens a dropdown panel with full notification details and timestamps. Notifications can be marked as read individually or all at once.

---

### 10. Citizen Identity & Profile

Citizens log in using **Aadhaar OTP verification** — their Aadhaar number is their primary, government-verified identity on the system. It cannot be changed inside the application.

On their profile page, citizens see:
- Their **Aadhaar number displayed prominently** (partially masked as `XXXX XXXX 5678` for security)
- A "Government of India · Unique Identification" badge confirming the verified source
- Editable contact details: Full Name, Mobile, Email, Address

This design ensures citizens always know which identity the system has on record for them, and that no one can impersonate them by changing the Aadhaar field.

---

## Who Uses This System

| Role | What They Do |
|---|---|
| **Citizen** | Logs in via Aadhaar OTP; views their land records; initiates sale requests as seller; consents to (or rejects) sale requests as buyer; tracks registration status; gets real-time notifications; downloads encumbrance certificates |
| **Data Entry Officer** | Enters and updates land records on behalf of the department; applies mutations |
| **Revenue Officer** | Reviews land records; records revenue payments; manages mutations; views registration history |
| **SRO Assistant** | Reviews buyer-consented registrations (PENDING_REVIEW); requests revisions from seller or forwards to the SRO for final approval; receives notifications for new and resubmitted cases |
| **Sub-Registrar Officer (SRO)** | Final authority on registrations; approves or rejects cases forwarded by SRO Assistant; can also create staff-initiated registrations directly; receives notifications when cases reach final approval stage |
| **Admin** | Manages users and roles; full access to all records, registrations, mutations, and revenue data |

---

## Key Benefits

**For Citizens**
- Know exactly who owns land before buying — no more fraudulent double sales
- Initiate a sale entirely from their phone — no office visit required
- Buyer must actively consent before any sale is processed — protects against forged transfers
- Real-time notifications at every step — no need to call the office or check repeatedly
- Track your registration application status online
- Get an Encumbrance Certificate in minutes instead of days
- See the exact boundary of any land parcel on a map
- Aadhaar-verified identity prevents impersonation

**For the Government**
- A single, authoritative digital record replaces paper-based registrars
- Complete audit trail of every change — who did what and when
- Checker-Maker workflow ensures no registration is processed without both parties' verified consent
- SRO Assistants act as a first line of review, reducing the SRO's workload
- Automatic land record updates on approval — no manual post-registration steps
- Reduced disputes and court cases from unclear ownership
- Faster revenue collection through transparent payment tracking

**For Banks and Financial Institutions**
- Reliable, up-to-date encumbrance certificates for loan processing
- Verified ownership data before disbursing land-backed loans

---

## How Fraud is Prevented

- **Identity verification** using Aadhaar OTP before any registration is submitted — ensures the person filing is who they claim to be
- **Buyer consent required** — no sale can be registered without the buyer explicitly logging in with their verified Aadhaar identity and consenting. A fraudster cannot register a property in someone else's name without that person's active participation.
- **SRO Assistant review layer** — an independent officer checks all documents before the final approver even sees the case
- **Immutable ownership history** — once a transfer is recorded and approved, it cannot be erased, only corrected through a formal process
- **Map-based boundary drawing** — the exact corners of each land parcel are drawn on a map and stored digitally, making boundary disputes verifiable
- **PLUS Code** — every parcel gets a globally unique location code that cannot be duplicated or faked
- **Optional blockchain anchoring** — registrations can be anchored to a public blockchain, making them cryptographically verifiable by anyone, anywhere, forever

---

## How Blockchain Works in This System

### The Simple Idea

Think of blockchain like a **public notice board that nobody can erase**.

When a property registration is approved, the system can optionally "stamp" that approval onto a blockchain — a shared digital ledger that is maintained by thousands of computers around the world simultaneously. Once something is written on this ledger, no single person, company, or government official can go back and change or delete it.

This is the difference between:
- A land record stored in a government database — *a file that an insider could theoretically alter*
- A land record anchored on a blockchain — *a permanent, public, tamper-proof stamp that anyone can verify independently*

---

### A Real-World Example

**The Situation:**
Ravi owns 3 acres of agricultural land in Nalgonda district (Survey No. 142/B). He wants to sell it to Priya for ₹45 lakhs.

**What Happens Step by Step:**

**Step 1 — Ravi initiates the sale request**
Ravi logs into the portal with his Aadhaar OTP. He selects the land parcel he owns and fills in Priya's details (name, Aadhaar, contact). A sale request is created in `DRAFT` status.

**Step 2 — Ravi sends the request to Priya**
Ravi reviews the details and clicks "Send to Buyer for Approval." The status moves to `AWAITING_BUYER_APPROVAL`. Priya receives a notification on her bell icon.

**Step 3 — Priya consents**
Priya logs in with her Aadhaar OTP and sees the sale request. She reviews the survey number, area, and amount. She clicks "I Consent to This Sale." The status moves to `PENDING_REVIEW`. All SRO Assistants are notified.

**Step 4 — SRO Assistant reviews**
An SRO Assistant opens the case, checks the documents, and verifies there are no disputes. They click "Forward to SRO." The status moves to `PENDING_APPROVAL`. All SROs are notified.

**Step 5 — SRO gives final approval**
The Sub-Registrar Officer reviews the case and clicks "Approve Registration." The system simultaneously:
- Sets the status to `APPROVED`
- Updates Priya as the new owner of Survey No. 142/B in the land records
- Refreshes the land map boundary with Priya's ownership
- Anchors a digital fingerprint of the registration to the blockchain (e.g., `0x8f3a...c91b`)
- Sends a notification to Ravi ("Sale approved — Priya is now the owner") and to Priya ("Land ownership transferred to you")

---

### What This Means in Practice

**Six months later**, someone tries to fraudulently re-sell the same land. The land records clearly show Priya as the owner. Even if someone tried to alter the government database, the blockchain entry with transaction hash `0x8f3a...c91b` is still publicly visible. Anyone — a court, a bank, another government — can look up that hash and verify:

- What land was transferred
- From whom to whom
- At exactly what date and time
- That the record has not been altered since

**The fraud is provably impossible.**

---

### What a Bank Sees

When Priya later goes to a bank to take a loan against this land, the bank officer can:

1. Pull the Encumbrance Certificate from this system — showing Priya as the owner with a clean title
2. Cross-check the blockchain transaction hash — confirming the registration is genuine and unaltered
3. Approve the loan with confidence — knowing the ownership is cryptographically verified

---

### Is Blockchain Always On?

No — it is **optional** and can be switched on or off by the government. When switched off, the system works exactly the same way for citizens and officers. The only difference is that approvals are not additionally anchored to the blockchain.

Blockchain anchoring is **turned off by default** for local and testing environments, and can be enabled for production deployments where the highest level of tamper-proof verification is needed.

---

## The Big Picture

This system transforms land governance from a process that happens in paper files inside government offices into a **transparent, citizen-accessible, digitally verifiable record** — bringing Telangana's land administration into the modern era and protecting citizens' most valuable asset: their land.

Every step of every transaction is visible, auditable, and fraud-resistant. Citizens are no longer passive subjects of a paper process — they are **active, verified participants** whose consent is required and recorded at every stage.

---

*Built by DivaTech · [divatech.in](https://www.divatech.in) · © 2026 All Rights Reserved*
