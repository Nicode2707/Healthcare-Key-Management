// SPDX-License-Identifier: MIT
pragma solidity ^0.8.34;

contract KeyLifecycleRegistry {

    // =========================================================
    // Owner
    // =========================================================

    address public owner;


    // =========================================================
    // Key Lifecycle Event Types
    // =========================================================

    enum EventType {
        CREATED,
        ROTATED,
        REVOKED,
        ARCHIVED,
        EXPIRED,
        RECOVERED
    }


    // =========================================================
    // Lifecycle Record
    // =========================================================

    struct LifecycleRecord {

        string keyId;

        uint256 version;

        EventType eventType;

        bytes32 recordHash;

        uint256 timestamp;

        address recordedBy;
    }


    // =========================================================
    // Storage
    // =========================================================

    mapping(bytes32 => LifecycleRecord[]) private keyEvents;

    uint256 public totalRecords;


    // =========================================================
    // Events
    // =========================================================

    event KeyLifecycleRecorded(
        bytes32 indexed keyIdHash,
        string keyId,
        uint256 version,
        EventType eventType,
        bytes32 recordHash,
        uint256 timestamp,
        address indexed recordedBy
    );


    // =========================================================
    // Access Control
    // =========================================================

    modifier onlyOwner() {

        require(
            msg.sender == owner,
            "Only owner can record lifecycle events"
        );

        _;
    }


    // =========================================================
    // Constructor
    // =========================================================

    constructor() {

        owner = msg.sender;
    }


    // =========================================================
    // Record Key Lifecycle Event
    // =========================================================

    function recordKeyEvent(
        string calldata keyId,
        uint256 version,
        EventType eventType,
        bytes32 recordHash
    )
        external
        onlyOwner
    {

        require(
            bytes(keyId).length > 0,
            "Key ID cannot be empty"
        );

        require(
            version > 0,
            "Version must be greater than zero"
        );

        require(
            recordHash != bytes32(0),
            "Record hash cannot be empty"
        );


        bytes32 keyIdHash =
            keccak256(bytes(keyId));


        keyEvents[keyIdHash].push(
            LifecycleRecord({
                keyId: keyId,
                version: version,
                eventType: eventType,
                recordHash: recordHash,
                timestamp: block.timestamp,
                recordedBy: msg.sender
            })
        );


        totalRecords++;


        emit KeyLifecycleRecorded(
            keyIdHash,
            keyId,
            version,
            eventType,
            recordHash,
            block.timestamp,
            msg.sender
        );
    }


    // =========================================================
    // Get Number of Events for a Key
    // =========================================================

    function getEventCount(
        string calldata keyId
    )
        external
        view
        returns (uint256)
    {

        bytes32 keyIdHash =
            keccak256(bytes(keyId));

        return keyEvents[keyIdHash].length;
    }


    // =========================================================
    // Get Specific Lifecycle Event
    // =========================================================

    function getKeyEvent(
        string calldata keyId,
        uint256 index
    )
        external
        view
        returns (
            string memory,
            uint256,
            EventType,
            bytes32,
            uint256,
            address
        )
    {

        bytes32 keyIdHash =
            keccak256(bytes(keyId));


        require(
            index < keyEvents[keyIdHash].length,
            "Event index out of range"
        );


        LifecycleRecord memory record =
            keyEvents[keyIdHash][index];


        return (
            record.keyId,
            record.version,
            record.eventType,
            record.recordHash,
            record.timestamp,
            record.recordedBy
        );
    }


    // =========================================================
    // Transfer Ownership
    // =========================================================

    function transferOwnership(
        address newOwner
    )
        external
        onlyOwner
    {

        require(
            newOwner != address(0),
            "New owner cannot be zero address"
        );

        owner = newOwner;
    }
}