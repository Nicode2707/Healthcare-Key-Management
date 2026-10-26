import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

export default buildModule("KeyLifecycleRegistryModule", (m) => {
const keyLifecycleRegistry = m.contract("KeyLifecycleRegistry");

return { keyLifecycleRegistry };
});