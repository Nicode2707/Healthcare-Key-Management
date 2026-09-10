import { expect } from "chai";
import { network } from "hardhat";

const { ethers } = await network.create();

describe("KeyLifecycleRegistry", function () {

  // =========================================================
  // Test 1: Owner
  // =========================================================

  it("Should set the deployer as the owner", async function () {

    const [deployer] = await ethers.getSigners();

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    expect(await registry.owner())
      .to.equal(deployer.address);
  });


  // =========================================================
  // Test 2: Record CREATED lifecycle event
  // =========================================================

  it("Should record a CREATED lifecycle event", async function () {

    const [deployer] = await ethers.getSigners();

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const keyId = "IOT-KEY-001";
    const version = 1;
    const eventType = 0; // CREATED

    const recordHash =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "IOT-KEY-001|1|CREATED"
        )
      );


    const transaction =
      await registry.recordKeyEvent(
        keyId,
        version,
        eventType,
        recordHash
      );

    const receipt =
      await transaction.wait();


    expect(receipt).to.not.equal(null);


    const events =
      await registry.queryFilter(
        registry.filters.KeyLifecycleRecorded(),
        receipt!.blockNumber,
        receipt!.blockNumber
      );


    expect(events.length)
      .to.equal(1);


    const event = events[0];


    expect(event.args.keyIdHash)
      .to.equal(
        ethers.keccak256(
          ethers.toUtf8Bytes(keyId)
        )
      );

    expect(event.args.keyId)
      .to.equal(keyId);

    expect(event.args.version)
      .to.equal(1n);

    expect(event.args.eventType)
      .to.equal(0);

    expect(event.args.recordHash)
      .to.equal(recordHash);

    expect(event.args.timestamp)
      .to.be.greaterThan(0n);

    expect(event.args.recordedBy)
      .to.equal(deployer.address);
  });


  // =========================================================
  // Test 3: Event count
  // =========================================================

  it("Should correctly count lifecycle events", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const keyId = "IOT-KEY-002";

    const hash1 =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "IOT-KEY-002|1|CREATED"
        )
      );

    const hash2 =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "IOT-KEY-002|2|ROTATED"
        )
      );


    await registry.recordKeyEvent(
      keyId,
      1,
      0, // CREATED
      hash1
    );

    await registry.recordKeyEvent(
      keyId,
      2,
      1, // ROTATED
      hash2
    );


    expect(
      await registry.getEventCount(keyId)
    ).to.equal(2n);


    expect(
      await registry.totalRecords()
    ).to.equal(2n);
  });


  // =========================================================
  // Test 4: Retrieve lifecycle event
  // =========================================================

  it("Should retrieve a stored lifecycle event", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const keyId = "IOT-KEY-003";
    const version = 1;
    const eventType = 0; // CREATED

    const recordHash =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "IOT-KEY-003|1|CREATED"
        )
      );


    await registry.recordKeyEvent(
      keyId,
      version,
      eventType,
      recordHash
    );


    const record =
      await registry.getKeyEvent(
        keyId,
        0
      );


    expect(record[0])
      .to.equal(keyId);

    expect(record[1])
      .to.equal(1n);

    expect(record[2])
      .to.equal(0);

    expect(record[3])
      .to.equal(recordHash);

    expect(record[4])
      .to.be.greaterThan(0n);

    expect(record[5])
      .to.equal(
        (await ethers.getSigners())[0].address
      );
  });


  // =========================================================
  // Test 5: Multiple lifecycle events
  // =========================================================

  it("Should maintain the correct lifecycle history", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const keyId = "IOT-KEY-004";


    const events = [
      {
        version: 1,
        type: 0, // CREATED
        data: "IOT-KEY-004|1|CREATED"
      },
      {
        version: 2,
        type: 1, // ROTATED
        data: "IOT-KEY-004|2|ROTATED"
      },
      {
        version: 2,
        type: 2, // REVOKED
        data: "IOT-KEY-004|2|REVOKED"
      },
      {
        version: 2,
        type: 3, // ARCHIVED
        data: "IOT-KEY-004|2|ARCHIVED"
      }
    ];


    for (const lifecycleEvent of events) {

      const recordHash =
        ethers.keccak256(
          ethers.toUtf8Bytes(
            lifecycleEvent.data
          )
        );


      await registry.recordKeyEvent(
        keyId,
        lifecycleEvent.version,
        lifecycleEvent.type,
        recordHash
      );
    }


    expect(
      await registry.getEventCount(keyId)
    ).to.equal(4n);


    expect(
      await registry.totalRecords()
    ).to.equal(4n);
  });


  // =========================================================
  // Test 6: Reject empty key ID
  // =========================================================

  it("Should reject an empty key ID", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const recordHash =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "EMPTY-KEY"
        )
      );


    await expect(
      registry.recordKeyEvent(
        "",
        1,
        0,
        recordHash
      )
    )
      .to.be.revertedWith(
        "Key ID cannot be empty"
      );
  });


  // =========================================================
  // Test 7: Reject invalid version
  // =========================================================

  it("Should reject version zero", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const recordHash =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "INVALID-VERSION"
        )
      );


    await expect(
      registry.recordKeyEvent(
        "IOT-KEY-005",
        0,
        0,
        recordHash
      )
    )
      .to.be.revertedWith(
        "Version must be greater than zero"
      );
  });


  // =========================================================
  // Test 8: Reject empty hash
  // =========================================================

  it("Should reject an empty record hash", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");


    await expect(
      registry.recordKeyEvent(
        "IOT-KEY-006",
        1,
        0,
        ethers.ZeroHash
      )
    )
      .to.be.revertedWith(
        "Record hash cannot be empty"
      );
  });


  // =========================================================
  // Test 9: Only owner can record events
  // =========================================================

  it("Should prevent a non-owner from recording lifecycle events", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const [, nonOwner] =
      await ethers.getSigners();


    const recordHash =
      ethers.keccak256(
        ethers.toUtf8Bytes(
          "NON-OWNER"
        )
      );


    await expect(
      registry
        .connect(nonOwner)
        .recordKeyEvent(
          "IOT-KEY-007",
          1,
          0,
          recordHash
        )
    )
      .to.be.revertedWith(
        "Only owner can record lifecycle events"
      );
  });


  // =========================================================
  // Test 10: Transfer ownership
  // =========================================================

  it("Should transfer ownership", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");

    const [, newOwner] =
      await ethers.getSigners();


    await registry.transferOwnership(
      newOwner.address
    );


    expect(
      await registry.owner()
    )
      .to.equal(newOwner.address);
  });


  // =========================================================
  // Test 11: Reject zero address ownership transfer
  // =========================================================

  it("Should reject transfer to the zero address", async function () {

    const registry =
      await ethers.deployContract("KeyLifecycleRegistry");


    await expect(
      registry.transferOwnership(
        ethers.ZeroAddress
      )
    )
      .to.be.revertedWith(
        "New owner cannot be zero address"
      );
  });

});