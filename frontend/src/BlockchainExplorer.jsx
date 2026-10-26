import { useCallback, useEffect, useState } from "react";
import {
RefreshCw,
Blocks,
ShieldCheck,
AlertTriangle,
Server,
UserRound,
Search,
FileCheck2,
Clock,
Hash,
} from "lucide-react";

const API_BASE_URL = "http://localhost:8080/api/blockchain";

const EVENT_NAMES = {
0: "CREATED",
1: "ROTATED",
2: "REVOKED",
3: "ARCHIVED",
4: "EXPIRED",
5: "RECOVERED",
};

function shortAddress(value) {
if (!value || typeof value !== "string") return "Not available";
if (value.length < 18) return value;
return `${value.slice(0, 10)}...${value.slice(-8)}`;
}

function formatTimestamp(value) {
if (value === null || value === undefined || value === "") {
return "Not available";
}

const numeric = Number(value);

// Smart-contract timestamps are generally Unix seconds.
const date = Number.isFinite(numeric)
? new Date(numeric * 1000)
: new Date(value);

return Number.isNaN(date.getTime())
? String(value)
: date.toLocaleString();
}

function eventName(event) {
if (event?.eventTypeName) return event.eventTypeName;

const type = event?.eventType;
if (type === null || type === undefined) return "UNKNOWN";

if (typeof type === "string" && Number.isNaN(Number(type))) {
return type;
}

return EVENT_NAMES[Number(type)] ?? `TYPE_${type}`;
}

export default function BlockchainExplorer() {
const [connection, setConnection] = useState(null);
const [owner, setOwner] = useState(null);
const [totalRecords, setTotalRecords] = useState(null);

const [keyId, setKeyId] = useState("KEY-VERIFY-001");
const [eventCount, setEventCount] = useState(null);
const [events, setEvents] = useState([]);

const [loading, setLoading] = useState(true);
const [searching, setSearching] = useState(false);
const [refreshing, setRefreshing] = useState(false);

const [errors, setErrors] = useState([]);
const [eventError, setEventError] = useState("");

const request = useCallback(async (path) => {
const token = localStorage.getItem("token");


if (!token) {
  throw new Error("Your session is missing. Please sign in again.");
}

const response = await fetch(`${API_BASE_URL}${path}`, {
  method: "GET",
  headers: {
    Accept: "application/json",
    Authorization: `Bearer ${token}`,
  },
});

if (response.status === 401) {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  window.location.assign("/login");
  throw new Error("Your session has expired.");
}

if (!response.ok) {
  const message = await response.text();
  throw new Error(
    message || `Request failed with HTTP ${response.status}.`,
  );
}

const text = await response.text();

if (!text) return null;

try {
  return JSON.parse(text);
} catch {
  return text.trim();
}


}, []);

const loadOverview = useCallback(
async (isRefresh = false) => {
if (isRefresh) {
setRefreshing(true);
} else {
setLoading(true);
}


  setErrors([]);

  const results = await Promise.allSettled([
    request("/connection"),
    request("/owner"),
    request("/records/count"),
  ]);

  const nextErrors = [];

  if (results[0].status === "fulfilled") {
    setConnection(results[0].value);
  } else {
    setConnection(null);
    nextErrors.push(
      `Blockchain connection: ${results[0].reason?.message || "Unavailable"}`,
    );
  }

  if (results[1].status === "fulfilled") {
    setOwner(results[1].value);
  } else {
    setOwner(null);
    nextErrors.push(
      `Contract owner: ${results[1].reason?.message || "Unavailable"}`,
    );
  }

  if (results[2].status === "fulfilled") {
    const value = results[2].value;
    setTotalRecords(
      value && typeof value === "object"
        ? (value.totalRecords ?? value.count ?? null)
        : value,
    );
  } else {
    setTotalRecords(null);
    nextErrors.push(
      `Total records: ${results[2].reason?.message || "Unavailable"}`,
    );
  }

  setErrors(nextErrors);
  setLoading(false);
  setRefreshing(false);
},
[request],


);

const searchLifecycleEvents = useCallback(
async (searchId = keyId) => {
const normalizedId = String(searchId || "").trim();


  if (!normalizedId) {
    setEventError("Enter a key ID to search.");
    return;
  }

  setSearching(true);
  setEventError("");
  setEventCount(null);
  setEvents([]);

  try {
    const encodedId = encodeURIComponent(normalizedId);

    const countResponse = await request(
      `/keys/${encodedId}/events/count`,
    );

    const count =
      countResponse && typeof countResponse === "object"
        ? Number(countResponse.eventCount ?? countResponse.count ?? 0)
        : Number(countResponse ?? 0);

    if (!Number.isFinite(count) || count < 0) {
      throw new Error("The event-count response has an unexpected format.");
    }

    setEventCount(count);

    if (count === 0) {
      setEvents([]);
      return;
    }

    // Fetch each event by its zero-based smart-contract index.
    const eventResults = await Promise.allSettled(
      Array.from({ length: count }, (_, index) =>
        request(`/keys/${encodedId}/events/${index}`),
      ),
    );

    const loadedEvents = [];
    const failedIndexes = [];

    eventResults.forEach((result, index) => {
      if (result.status === "fulfilled") {
        const payload = result.value;

        // Support either a direct event object or a wrapped response.
        const event =
          payload?.event && typeof payload.event === "object"
            ? payload.event
            : payload;

        loadedEvents.push({
          ...event,
          keyId: event?.keyId ?? normalizedId,
          index,
        });
      } else {
        failedIndexes.push(index);
      }
    });

    setEvents(loadedEvents);

    if (failedIndexes.length > 0) {
      setEventError(
        `Could not retrieve event index(es): ${failedIndexes.join(", ")}. Check the event-details API response.`,
      );
    }
  } catch (error) {
    if (error.message?.includes("session")) return;

    setEventError(
      error.message || "Unable to retrieve lifecycle events.",
    );
  } finally {
    setSearching(false);
  }
},
[keyId, request],


);

useEffect(() => {
loadOverview();
}, [loadOverview]);

const connectionAvailable = Boolean(connection);
const ownerText =
typeof owner === "string"
? owner
: owner?.owner ?? owner?.address ?? "";

const clientText =
typeof connection === "string"
? connection
: connection?.clientVersion ?? connection?.version ?? "";

return ( <main className="dashboard-content"> <div className="welcome-row"> <div> <span className="eyebrow"> <Blocks size={15} /> BLOCKCHAIN INFRASTRUCTURE </span>


      <h1>Blockchain Explorer</h1>

      <p className="muted">
        Monitor blockchain connectivity and inspect lifecycle evidence
        recorded for healthcare key metadata.
      </p>
    </div>

    <button
      className="button button-outline"
      type="button"
      onClick={() => loadOverview(true)}
      disabled={loading || refreshing}
    >
      <RefreshCw
        size={16}
        className={refreshing ? "blockchain-spin" : ""}
      />
      {refreshing ? "Refreshing..." : "Refresh status"}
    </button>
  </div>

  <div className="notice-banner">
    <ShieldCheck size={22} />

    <div>
      <strong>Blockchain-backed lifecycle verification</strong>
      <p>
        This page reads blockchain information through authenticated
        backend APIs. AES secret key material is never displayed.
      </p>
    </div>
  </div>

  {errors.length > 0 && (
    <div className="auth-error" role="alert" style={{ marginTop: 18 }}>
      <strong>Some blockchain information could not be loaded.</strong>
      <ul style={{ marginBottom: 0 }}>
        {errors.map((error) => (
          <li key={error}>{error}</li>
        ))}
      </ul>
    </div>
  )}

  <section
    className="stat-grid"
    aria-label="Blockchain statistics"
    style={{ marginTop: 22 }}
  >
    <article className="stat-card">
      <span className="stat-icon teal">
        <Server />
      </span>
      <span className="muted">Blockchain client</span>
      <strong style={{ fontSize: 19 }}>
        {loading
          ? "Loading..."
          : connectionAvailable
            ? "Connected"
            : "Unavailable"}
      </strong>
      <small>
        {connectionAvailable
          ? "Client responded successfully"
          : "Connection needs attention"}
      </small>
    </article>

    <article className="stat-card">
      <span className="stat-icon purple">
        <UserRound />
      </span>
      <span className="muted">Contract owner</span>
      <strong style={{ fontSize: 19 }}>
        {loading ? "Loading..." : ownerText ? "Retrieved" : "Unavailable"}
      </strong>
      <small>{ownerText ? shortAddress(ownerText) : "Not available"}</small>
    </article>

    <article className="stat-card">
      <span className="stat-icon green">
        <Blocks />
      </span>
      <span className="muted">Total blockchain records</span>
      <strong style={{ fontSize: 25 }}>
        {loading ? "..." : totalRecords ?? "Unavailable"}
      </strong>
      <small>Records stored in the smart contract</small>
    </article>

    <article className="stat-card">
      <span className={`stat-icon ${errors.length ? "orange" : "green"}`}>
        {errors.length ? <AlertTriangle /> : <ShieldCheck />}
      </span>
      <span className="muted">API health</span>
      <strong style={{ fontSize: 19 }}>
        {loading
          ? "Checking..."
          : errors.length
            ? "Partial / Failed"
            : "Responding"}
      </strong>
      <small>
        {loading
          ? "Checking blockchain endpoints"
          : errors.length
            ? `${errors.length} request(s) failed`
            : "All overview requests succeeded"}
      </small>
    </article>
  </section>

  <section className="panel" style={{ marginTop: 22 }}>
    <div className="panel-heading">
      <div>
        <h3>Connection details</h3>
        <p className="muted">
          Values returned by the Spring Boot backend
        </p>
      </div>
      <Blocks />
    </div>

    <div style={{ display: "grid", gap: 18, marginTop: 18 }}>
      <div>
        <span className="muted">Blockchain client version</span>
        <div
          style={{
            marginTop: 7,
            padding: 14,
            borderRadius: 10,
            border: "1px solid var(--border, #dce7e5)",
            overflowWrap: "anywhere",
            fontFamily: "monospace",
          }}
        >
          {loading ? "Loading..." : clientText || "Not available"}
        </div>
      </div>

      <div>
        <span className="muted">Smart contract owner address</span>
        <div
          style={{
            marginTop: 7,
            padding: 14,
            borderRadius: 10,
            border: "1px solid var(--border, #dce7e5)",
            overflowWrap: "anywhere",
            fontFamily: "monospace",
          }}
        >
          {loading ? "Loading..." : ownerText || "Not available"}
        </div>
      </div>
    </div>
  </section>

  <section className="panel" style={{ marginTop: 22 }}>
    <div className="panel-heading">
      <div>
        <h3>Lifecycle event verification</h3>
        <p className="muted">
          Search for a key and inspect its smart-contract event history.
        </p>
      </div>
      <FileCheck2 />
    </div>

    <form
      onSubmit={(event) => {
        event.preventDefault();
        searchLifecycleEvents();
      }}
      style={{
        display: "flex",
        flexWrap: "wrap",
        gap: 10,
        marginTop: 20,
      }}
    >
      <input
        value={keyId}
        onChange={(event) => setKeyId(event.target.value)}
        placeholder="Enter key ID, e.g. KEY-VERIFY-001"
        aria-label="Key ID"
        style={{
          flex: "1 1 260px",
          minWidth: 0,
          padding: "12px 14px",
          border: "1px solid var(--border, #dce7e5)",
          borderRadius: 10,
          background: "var(--surface, #ffffff)",
          color: "var(--text, #203b45)",
        }}
      />

      <button
        type="submit"
        className="button button-primary"
        disabled={searching || !keyId.trim()}
      >
        <Search size={16} />
        {searching ? "Searching..." : "Verify events"}
      </button>
    </form>

    {eventError && (
      <div className="auth-error" role="alert" style={{ marginTop: 16 }}>
        {eventError}
      </div>
    )}

    {eventCount !== null && (
      <div
        className="notice-banner"
        role="status"
        style={{ marginTop: 18 }}
      >
        <ShieldCheck size={22} />
        <div>
          <strong>
            {eventCount} event{eventCount === 1 ? "" : "s"} found for{" "}
            {keyId.trim()}
          </strong>
          <p>
            {eventCount > 0
              ? "Event details are retrieved from the blockchain backend."
              : "No lifecycle events were returned for this key ID."}
          </p>
        </div>
      </div>
    )}

    {events.length > 0 && (
      <div style={{ overflowX: "auto", marginTop: 20 }}>
        <table
          style={{
            width: "100%",
            borderCollapse: "collapse",
            textAlign: "left",
            minWidth: 720,
          }}
        >
          <thead>
            <tr>
              {[
                "Index",
                "Key ID",
                "Version",
                "Event",
                "Record hash",
                "Timestamp",
                "Recorded by",
              ].map((heading) => (
                <th
                  key={heading}
                  style={{
                    padding: "12px 10px",
                    borderBottom: "1px solid var(--border, #dce7e5)",
                    fontSize: 12,
                  }}
                >
                  {heading}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {events.map((event, position) => (
              <tr key={`${event.keyId}-${event.index ?? position}`}>
                <td style={{ padding: 10 }}>{event.index ?? position}</td>
                <td style={{ padding: 10 }}>{event.keyId ?? keyId}</td>
                <td style={{ padding: 10 }}>
                  {event.version ?? "—"}
                </td>
                <td style={{ padding: 10 }}>
                  <span className="environment-badge">
                    {eventName(event)}
                  </span>
                </td>
                <td
                  style={{
                    padding: 10,
                    fontFamily: "monospace",
                    overflowWrap: "anywhere",
                  }}
                >
                  {event.recordHash ?? event.record_hash ?? "—"}
                </td>
                <td style={{ padding: 10 }}>
                  <span style={{ display: "inline-flex", gap: 6 }}>
                    <Clock size={15} />
                    {formatTimestamp(
                      event.timestamp ?? event.createdAt ?? event.time,
                    )}
                  </span>
                </td>
                <td
                  style={{
                    padding: 10,
                    fontFamily: "monospace",
                    overflowWrap: "anywhere",
                  }}
                >
                  {event.recordedBy ?? event.recorded_by ?? "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )}

    {eventCount === 0 && !searching && (
      <div className="empty-state">
        <div className="empty-icon">
          <Hash size={25} />
        </div>
        <strong>No lifecycle events found</strong>
        <p>
          Check the key ID or confirm that its lifecycle event was recorded
          on the current blockchain deployment.
        </p>
      </div>
    )}

    {eventCount === null && !searching && !eventError && (
      <div className="empty-state">
        <div className="empty-icon">
          <Blocks size={25} />
        </div>
        <strong>Ready to verify lifecycle events</strong>
        <p>
          Search for KEY-VERIFY-001 or enter another key ID to load its
          blockchain history.
        </p>
      </div>
    )}
  </section>
</main>


);
}
