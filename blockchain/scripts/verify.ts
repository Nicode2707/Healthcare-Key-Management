import { network } from "hardhat";

const { ethers } = await network.create();

async function main() {

  console.log("========================================");
  console.log("KeyLifecycleRegistry Verification");
  console.log("========================================");


  // =========================================================
  // Get accounts
  // =========================================================

  const [deployer] =
    await ethers.getSigners();

  console.log(
    "Deployer:",
    deployer.address
  );


  // =========================================================
  // Deploy contract
  // =========================================================

  console.log("\nDeploying contract...");

  const registry =
    await ethers.deployContract(
      "KeyLifecycleRegistry"
    );

  await registry.waitForDeployment();

  const contractAddress =
    await registry.getAddress();

  console.log(
    "Contract address:",
    contractAddress
  );


  // =========================================================
  // Verify owner
  // =========================================================

  const owner =
    await registry.owner();

  console.log(
    "\nOwner:",
    owner
  );

  if (owner !== deployer.address) {

    throw new Error(
      "Owner verification failed"
    );
  }

  console.log(
    "Owner verification: PASSED"
  );


  // =========================================================
  // Verify initial record count
  // =========================================================

  const initialTotal =
    await registry.totalRecords();

  console.log(
    "\nInitial total records:",
    initialTotal.toString()
  );

  if (initialTotal !== 0n) {

    throw new Error(
      "Initial record count should be zero"
    );
  }

  console.log(
    "Initial record count verification: PASSED"
  );


  // =========================================================
  // Record CREATED event
  // =========================================================

  const keyId =
    "HEALTHCARE-IOT-001";

  const version = 1;

  const eventType = 0; // CREATED

  const recordHash =
    ethers.keccak256(
      ethers.toUtf8Bytes(
        "HEALTHCARE-IOT-001|1|CREATED"
      )
    );


  console.log(
    "\nRecording CREATED lifecycle event..."
  );


  const transaction =
    await registry.recordKeyEvent(
      keyId,
      version,
      eventType,
      recordHash
    );

  await transaction.wait();


  console.log(
    "CREATED event recorded"
  );


  // =========================================================
  // Verify total record count
  // =========================================================

  const totalRecords =
    await registry.totalRecords();

  console.log(
    "Total records:",
    totalRecords.toString()
  );

  if (totalRecords !== 1n) {

    throw new Error(
      "Total record count verification failed"
    );
  }

  console.log(
    "Total record count verification: PASSED"
  );


  // =========================================================
  // Verify key event count
  // =========================================================

  const eventCount =
    await registry.getEventCount(
      keyId
    );

  console.log(
    "Events for key:",
    eventCount.toString()
  );

  if (eventCount !== 1n) {

    throw new Error(
      "Key event count verification failed"
    );
  }

  console.log(
    "Key event count verification: PASSED"
  );


  // =========================================================
  // Retrieve lifecycle record
  // =========================================================

  const record =
    await registry.getKeyEvent(
      keyId,
      0
    );


  console.log(
    "\nRetrieved lifecycle record:"
  );

  console.log(
    "Key ID:",
    record[0]
  );

  console.log(
    "Version:",
    record[1].toString()
  );

  console.log(
    "Event Type:",
    record[2].toString()
  );

  console.log(
    "Record Hash:",
    record[3]
  );

  console.log(
    "Timestamp:",
    record[4].toString()
  );

  console.log(
    "Recorded By:",
    record[5]
  );


  // =========================================================
  // Verify retrieved data
  // =========================================================

  if (record[0] !== keyId) {

    throw new Error(
      "Key ID verification failed"
    );
  }


  if (record[1] !== 1n) {

    throw new Error(
      "Version verification failed"
    );
  }


  // Solidity enum values are returned as BigInt
  if (record[2] !== 0n) {

    throw new Error(
      "Event type verification failed"
    );
  }


  if (record[3] !== recordHash) {

    throw new Error(
      "Record hash verification failed"
    );
  }


  if (record[4] <= 0n) {

    throw new Error(
      "Timestamp verification failed"
    );
  }


  if (record[5] !== deployer.address) {

    throw new Error(
      "Recorded-by verification failed"
    );
  }


  console.log(
    "\nLifecycle record verification: PASSED"
  );


  // =========================================================
  // Final result
  // =========================================================

  console.log("\n========================================");
  console.log("ALL VERIFICATIONS PASSED");
  console.log("========================================");

  console.log(
    "\nContract address:",
    contractAddress
  );

  console.log(
    "Contract owner:",
    owner
  );

  console.log(
    "Total lifecycle records:",
    totalRecords.toString()
  );
}


main().catch((error) => {

  console.error(error);

  process.exitCode = 1;
});