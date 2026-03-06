// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/// @title Land Registry proof contract for registration + mutation workflow
/// @notice Stores minimal metadata hashes and lifecycle states for auditability
contract LandRegistry {
    enum RecordStatus {
        None,
        Registered,
        MutationPending,
        MutationApproved,
        Disputed
    }

    struct LandRecord {
        bytes32 parcelId;
        address currentOwner;
        bytes32 deedHash;
        string registrationRef;
        uint256 updatedAt;
        RecordStatus status;
    }

    address public registrarAdmin;

    mapping(address => bool) public registrars;
    mapping(address => bool) public mutationOfficers;
    mapping(bytes32 => LandRecord) public records;

    event RegistrarUpdated(address indexed registrar, bool enabled);
    event MutationOfficerUpdated(address indexed officer, bool enabled);

    event Registered(
        bytes32 indexed parcelId,
        address indexed owner,
        bytes32 deedHash,
        string registrationRef,
        uint256 timestamp
    );

    event MutationRequested(
        bytes32 indexed parcelId,
        address indexed fromOwner,
        address indexed toOwner,
        bytes32 mutationFileHash,
        uint256 timestamp
    );

    event MutationApproved(
        bytes32 indexed parcelId,
        address indexed newOwner,
        bytes32 orderHash,
        uint256 timestamp
    );

    event StatusMarkedDisputed(bytes32 indexed parcelId, uint256 timestamp);

    modifier onlyAdmin() {
        require(msg.sender == registrarAdmin, "only admin");
        _;
    }

    modifier onlyRegistrar() {
        require(registrars[msg.sender], "only registrar");
        _;
    }

    modifier onlyMutationOfficer() {
        require(mutationOfficers[msg.sender], "only mutation officer");
        _;
    }

    constructor() {
        registrarAdmin = msg.sender;
        registrars[msg.sender] = true;
    }

    function setRegistrar(address account, bool enabled) external onlyAdmin {
        registrars[account] = enabled;
        emit RegistrarUpdated(account, enabled);
    }

    function setMutationOfficer(address account, bool enabled) external onlyAdmin {
        mutationOfficers[account] = enabled;
        emit MutationOfficerUpdated(account, enabled);
    }

    function registerLand(
        bytes32 parcelId,
        address owner,
        bytes32 deedHash,
        string calldata registrationRef
    ) external onlyRegistrar {
        require(parcelId != bytes32(0), "invalid parcel");
        require(owner != address(0), "invalid owner");
        require(bytes(registrationRef).length > 0, "empty registration ref");

        LandRecord storage r = records[parcelId];
        require(r.status == RecordStatus.None, "already exists");

        records[parcelId] = LandRecord({
            parcelId: parcelId,
            currentOwner: owner,
            deedHash: deedHash,
            registrationRef: registrationRef,
            updatedAt: block.timestamp,
            status: RecordStatus.Registered
        });

        emit Registered(parcelId, owner, deedHash, registrationRef, block.timestamp);
    }

    function requestMutation(
        bytes32 parcelId,
        address toOwner,
        bytes32 mutationFileHash
    ) external {
        LandRecord storage r = records[parcelId];
        require(r.status == RecordStatus.Registered || r.status == RecordStatus.MutationApproved, "invalid state");
        require(msg.sender == r.currentOwner || registrars[msg.sender], "unauthorized caller");
        require(toOwner != address(0), "invalid owner");

        r.status = RecordStatus.MutationPending;
        r.updatedAt = block.timestamp;

        emit MutationRequested(parcelId, r.currentOwner, toOwner, mutationFileHash, block.timestamp);
    }

    function approveMutation(
        bytes32 parcelId,
        address newOwner,
        bytes32 mutationOrderHash
    ) external onlyMutationOfficer {
        LandRecord storage r = records[parcelId];
        require(r.status == RecordStatus.MutationPending, "not pending");
        require(newOwner != address(0), "invalid new owner");

        r.currentOwner = newOwner;
        r.updatedAt = block.timestamp;
        r.status = RecordStatus.MutationApproved;

        emit MutationApproved(parcelId, newOwner, mutationOrderHash, block.timestamp);
    }

    function markDisputed(bytes32 parcelId) external onlyMutationOfficer {
        LandRecord storage r = records[parcelId];
        require(r.status != RecordStatus.None, "record missing");

        r.status = RecordStatus.Disputed;
        r.updatedAt = block.timestamp;

        emit StatusMarkedDisputed(parcelId, block.timestamp);
    }

    function getRecord(bytes32 parcelId) external view returns (LandRecord memory) {
        return records[parcelId];
    }
}
