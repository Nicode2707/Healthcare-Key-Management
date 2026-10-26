import { useState } from 'react'
import {
  AlertCircle,
  CheckCircle2,
  Clock3,
  KeyRound,
  RefreshCw,
  ShieldCheck,
} from 'lucide-react'

import {
  createRecoveryRequest,
  approveRecoveryRequest,
  executeRecoveryRequest,
} from './api/keyRecoveryApi.js'

const cardStyle = {
  background: 'var(--surface, #ffffff)',
  border: '1px solid var(--border, #dce7e5)',
  borderRadius: 16,
  padding: 22,
}

const inputStyle = {
  display: 'block',
  width: '100%',
  boxSizing: 'border-box',
  marginTop: 8,
  padding: '12px 14px',
  border: '1px solid var(--border, #dce7e5)',
  borderRadius: 10,
  background: 'var(--surface, #ffffff)',
  color: 'var(--text, #203b45)',
  font: 'inherit',
}

const buttonStyle = {
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  gap: 8,
  padding: '11px 15px',
  border: 0,
  borderRadius: 10,
  background: 'var(--primary, #008f83)',
  color: '#ffffff',
  fontWeight: 600,
  cursor: 'pointer',
}

const secondaryButtonStyle = {
  ...buttonStyle,
  background: 'var(--surface-muted, #eaf6f3)',
  color: 'var(--text, #203b45)',
  border: '1px solid var(--border, #dce7e5)',
}

function getErrorMessage(error) {
  const data = error?.response?.data

  if (data?.message) return data.message
  if (data?.error) return data.error

  switch (error?.response?.status) {
    case 400:
      return 'The recovery request contains invalid information.'
    case 401:
      return 'Your session may have expired. Please sign in again.'
    case 403:
      return 'Administrator permissions are required.'
    case 404:
      return 'The key or recovery request was not found.'
    case 409:
      return 'The current key or recovery status does not permit this operation.'
    default:
      return error?.response
        ? 'The operation failed. Please try again.'
        : 'Cannot connect to the backend. Check that Spring Boot is running.'
  }
}

export default function KeyRecoveryPanel({ role }) {
  const isAdmin = String(role || '').toUpperCase() === 'ADMIN'

  const [keyId, setKeyId] = useState('')
  const [reason, setReason] = useState('')
  const [requestId, setRequestId] = useState('')

  const [latestResponse, setLatestResponse] = useState(null)
  const [loadingAction, setLoadingAction] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const busy = Boolean(loadingAction)

  async function runAction(actionName, operation) {
    setLoadingAction(actionName)
    setErrorMessage('')
    setSuccessMessage('')

    try {
      const result = await operation()

      setLatestResponse((previous) => ({
        ...(previous || {}),
        ...result,
      }))

      if (result?.requestId != null) {
        setRequestId(String(result.requestId))
      }

      setSuccessMessage(
        result?.message || 'Operation completed successfully.'
      )
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setLoadingAction('')
    }
  }

  if (!isAdmin) {
    return (
      <section style={cardStyle}>
        <ShieldCheck size={26} />

        <h2>Emergency Key Recovery</h2>

        <p>
          Emergency recovery operations are restricted to administrators.
          Contact your administrator if a key requires recovery.
        </p>
      </section>
    )
  }

  return (
    <div style={{ display: 'grid', gap: 18 }}>
      {/* CREATE RECOVERY REQUEST */}
      <section style={cardStyle}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            marginBottom: 20,
          }}
        >
          <RefreshCw size={25} />

          <div>
            <span className="eyebrow">ADMINISTRATION</span>
            <h2 style={{ margin: '5px 0' }}>
              Emergency Key Recovery
            </h2>

            <p style={{ margin: 0, opacity: 0.75 }}>
              Create a recovery request for an eligible key.
            </p>
          </div>
        </div>

        <form
          onSubmit={(event) => {
            event.preventDefault()

            runAction('create', () =>
              createRecoveryRequest(
                keyId.trim(),
                reason.trim()
              )
            )
          }}
        >
          <label style={{ display: 'block', marginBottom: 16 }}>
            Key ID

            <input
              style={inputStyle}
              value={keyId}
              onChange={(event) => setKeyId(event.target.value)}
              placeholder="Enter the existing key ID"
              required
              disabled={busy}
            />
          </label>

          <label style={{ display: 'block', marginBottom: 18 }}>
            Recovery reason

            <textarea
              style={{
                ...inputStyle,
                minHeight: 100,
                resize: 'vertical',
              }}
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Explain why emergency recovery is required"
              maxLength={500}
              required
              disabled={busy}
            />
          </label>

          <button
            type="submit"
            style={buttonStyle}
            disabled={busy || !keyId.trim() || !reason.trim()}
          >
            <KeyRound size={17} />

            {loadingAction === 'create'
              ? 'Submitting request...'
              : 'Create Recovery Request'}
          </button>
        </form>
      </section>

      {/* APPROVE AND EXECUTE */}
      <section style={cardStyle}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            marginBottom: 18,
          }}
        >
          <Clock3 size={24} />

          <div>
            <h3 style={{ margin: 0 }}>
              Approve and Execute Recovery
            </h3>

            <p style={{ marginBottom: 0, opacity: 0.75 }}>
              Enter the request ID returned by the backend.
            </p>
          </div>
        </div>

        <label style={{ display: 'block' }}>
          Recovery Request ID

          <input
            style={inputStyle}
            value={requestId}
            onChange={(event) => setRequestId(event.target.value)}
            placeholder="Enter recovery request ID"
            required
            disabled={busy}
          />
        </label>

        <div
          style={{
            display: 'flex',
            gap: 10,
            flexWrap: 'wrap',
            marginTop: 16,
          }}
        >
          <button
            type="button"
            style={secondaryButtonStyle}
            disabled={busy || !requestId.trim()}
            onClick={() =>
              runAction('approve', () =>
                approveRecoveryRequest(requestId.trim())
              )
            }
          >
            <CheckCircle2 size={17} />

            {loadingAction === 'approve'
              ? 'Approving...'
              : 'Approve Request'}
          </button>

          <button
            type="button"
            style={buttonStyle}
            disabled={busy || !requestId.trim()}
            onClick={() => {
              const confirmed = window.confirm(
                'Execute this recovery request? Continue only after verifying that the request has been approved.'
              )

              if (confirmed) {
                runAction('execute', () =>
                  executeRecoveryRequest(requestId.trim())
                )
              }
            }}
          >
            <ShieldCheck size={17} />

            {loadingAction === 'execute'
              ? 'Executing...'
              : 'Execute Recovery'}
          </button>
        </div>
      </section>

      {/* API FEEDBACK */}
      {errorMessage && (
        <section
          role="alert"
          style={{
            ...cardStyle,
            display: 'flex',
            gap: 10,
            color: '#a52828',
          }}
        >
          <AlertCircle size={20} />
          <span>{errorMessage}</span>
        </section>
      )}

      {successMessage && (
        <section
          role="status"
          style={{
            ...cardStyle,
            display: 'flex',
            gap: 10,
            color: '#13724d',
          }}
        >
          <CheckCircle2 size={20} />
          <span>{successMessage}</span>
        </section>
      )}

      {/* LATEST RECOVERY RESPONSE */}
      <section style={cardStyle}>
        <h3>Latest Recovery Response</h3>

        {latestResponse ? (
          <dl
            style={{
              display: 'grid',
              gridTemplateColumns:
                'repeat(auto-fit, minmax(180px, 1fr))',
              gap: 16,
            }}
          >
            {[
              ['Request ID', latestResponse.requestId],
              ['Key ID', latestResponse.keyId],
              ['Status', latestResponse.recoveryStatus],
              ['Requested By', latestResponse.requestedBy],
              ['Approved By', latestResponse.approvedBy],
              ['Executed By', latestResponse.executedBy],
              ['Reason', latestResponse.reason],
              ['Requested At', latestResponse.requestedAt],
              ['Processed At', latestResponse.processedAt],
            ]
              .filter(
                ([, value]) =>
                  value !== undefined &&
                  value !== null &&
                  value !== ''
              )
              .map(([label, value]) => (
                <div key={label}>
                  <dt style={{ fontSize: 12, opacity: 0.7 }}>
                    {label}
                  </dt>

                  <dd
                    style={{
                      margin: '5px 0 0',
                      overflowWrap: 'anywhere',
                    }}
                  >
                    {String(value)}
                  </dd>
                </div>
              ))}
          </dl>
        ) : (
          <p style={{ opacity: 0.75 }}>
            Recovery details will appear here after the first successful
            API operation.
          </p>
        )}

        <p style={{ fontSize: 12, opacity: 0.7, marginBottom: 0 }}>
          Recovery records shown here come from actual API responses.
          They are not a complete recovery history.
        </p>
      </section>
    </div>
  )
}