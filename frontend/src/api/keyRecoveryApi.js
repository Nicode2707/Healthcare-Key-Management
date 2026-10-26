import api from './axios'

export const createRecoveryRequest = async (keyId, reason) => {
  const response = await api.post(
    `/api/admin/keys/${encodeURIComponent(keyId)}/recover`,
    { reason }
  )

  return response.data
}

export const approveRecoveryRequest = async (requestId) => {
  const response = await api.post(
    `/api/admin/keys/recovery/${encodeURIComponent(requestId)}/approve`
  )

  return response.data
}

export const executeRecoveryRequest = async (requestId) => {
  const response = await api.post(
    `/api/admin/keys/recovery/${encodeURIComponent(requestId)}/execute`
  )

  return response.data
}