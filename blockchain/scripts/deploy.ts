import { network } from "hardhat";

const { ethers } = await network.create();

async function main() {

  console.log("Deploying KeyLifecycleRegistry...");

  const [deployer] =
    await ethers.getSigners();

  console.log(
    "Deployer address:",
    deployer.address
  );

  const registry =
    await ethers.deployContract(
      "KeyLifecycleRegistry"
    );

  await registry.waitForDeployment();

  const contractAddress =
    await registry.getAddress();

  console.log(
    "KeyLifecycleRegistry deployed at:",
    contractAddress
  );

  console.log(
    "Contract owner:",
    await registry.owner()
  );
}

main().catch((error) => {

  console.error(error);

  process.exitCode = 1;
});