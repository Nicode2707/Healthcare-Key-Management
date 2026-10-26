import api from "./axios";

const API_BASE_URL = "/api/keys";

/**
 * Fetch all key metadata.
 */
export const getKeys = async () => {
  const response = await api.get(API_BASE_URL);
  const data = response.data;

  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.keys)) return data.keys;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.content)) return data.content;

  return [];
};

/**
 * Fetch all keys.
 */
export const getAllKeys = async () => {
  return getKeys();
};

/**
 * Calculate key statistics.
 */
export const calculateKeyStatistics = (keys = []) => {
  const total = keys.length;

  const active = keys.filter(
    (key) => key.status?.toUpperCase() === "ACTIVE"
  ).length;

  const revoked = keys.filter(
    (key) => key.status?.toUpperCase() === "REVOKED"
  ).length;

  const rotated = keys.filter(
    (key) => key.status?.toUpperCase() === "ROTATED"
  ).length;

  const archived = keys.filter(
    (key) => key.status?.toUpperCase() === "ARCHIVED"
  ).length;

  const expired = keys.filter(
    (key) => key.status?.toUpperCase() === "EXPIRED"
  ).length;

  return {
    total,
    active,
    revoked,
    rotated,
    archived,
    expired,
  };
};

/**
 * Create an AES-256 key.
 */
export const createKey = async (keyData) => {
  const payload = {
    ...keyData,
    algorithm: "AES-256",
  };

  const response = await api.post(API_BASE_URL, payload);

  return response.data;
};

/**
 * Rotate an existing key.
 */
export const rotateKey = async (keyId) => {
  const response = await api.patch(
    `${API_BASE_URL}/${encodeURIComponent(keyId)}/rotate`
  );

  return response.data;
};

/**
 * Revoke an active key.
 */
export const revokeKey = async (keyId) => {
  const response = await api.patch(
    `${API_BASE_URL}/${encodeURIComponent(keyId)}/revoke`
  );

  return response.data;
};

/**
 * Archive eligible non-active versions of a key.
 *
 * The backend archives eligible ROTATED, REVOKED,
 * or EXPIRED versions. Archival is rejected if an
 * ACTIVE version exists for the same keyId.
 *
 * Returns the number of archived versions.
 */
export const archiveKey = async (keyId) => {
  const response = await api.patch(
    `${API_BASE_URL}/${encodeURIComponent(keyId)}/archive`
  );

  return response.data;
};

