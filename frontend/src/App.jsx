import KeyRecoveryPanel from "./KeyRecoveryPanel";
import BlockchainExplorer from "./BlockchainExplorer";
import { useEffect, useState } from "react";
import {
  BrowserRouter,
  Link,
  NavLink,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useLocation,
} from "react-router-dom";
import {
  Activity,
  ArrowRight,
  Blocks,
  CheckCircle2,
  ChevronDown,
  KeyRound,
  LayoutDashboard,
  LockKeyhole,
  Menu,
  Moon,
  RefreshCw,
  Search,
  ShieldCheck,
  Plus,
  RotateCw,
  Ban,
  Sun,
  X,
} from "lucide-react";
import { loginUser, registerUser } from "./api/authApi";
import { getKeys, createKey, rotateKey, revokeKey, archiveKey } from "./api/keyApi";
import "./App.css";

// =========================================================
// BRAND
// =========================================================

function Brand() {
  return (
    <Link to="/" className="brand" aria-label="KeyShield home">
      <span className="brand-icon">
        <ShieldCheck size={23} />
      </span>

      <span>KeyShield</span>
    </Link>
  );
}

// =========================================================
// FOOTER
// =========================================================

function Footer({ onTeam }) {
  return (
    <footer className="footer">
      <span>Copyright © 2026 Nicode2707. All rights reserved.</span>

      <button type="button" className="footer-link" onClick={onTeam}>
        Project Team
      </button>
    </footer>
  );
}

// =========================================================
// PROJECT TEAM MODAL
// =========================================================

function TeamModal({ onClose }) {
  const teamMembers = [
    {
      name: "Niraj Kumar",
      role: "Team Leader",
      contribution: "Overall system architecture and backend development",
    },
    {
      name: "Piyush Kumar",
      contribution: "Authentication & Authorization",
    },
    {
      name: "Kanhaiya Kumar",
      contribution: "Database Design & Handling",
    },
    {
      name: "Prashant Kumar",
      contribution: "Blockchain Integration",
    },
  ];

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        className="team-modal"
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="team-modal-title"
        style={{ maxWidth: "760px", maxHeight: "90vh", overflowY: "auto" }}
      >
        <button
          className="icon-button modal-close"
          onClick={onClose}
          aria-label="Close project team"
          type="button"
        >
          <X size={20} />
        </button>

        <span className="eyebrow">PROJECT INFORMATION</span>

        <h2 id="team-modal-title">KeyShield</h2>

        <p className="muted">
          Blockchain-Assisted Distributed AES Key Lifecycle Management Framework
          for Secure Healthcare IoT.
        </p>

        <div className="team-note">
          <ShieldCheck size={20} />

          <p>
            Designed to support secure cryptographic key lifecycle management,
            controlled access and blockchain-backed integrity evidence for
            healthcare systems.
          </p>
        </div>

        <div style={{ marginTop: "28px" }}>
          <span className="eyebrow">PROJECT TEAM MEMBERS</span>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "minmax(130px, 0.9fr) minmax(0, 1.5fr)",
              marginTop: "14px",
              border: "1px solid var(--border, #dce7e5)",
              borderRadius: "10px",
              overflow: "hidden",
            }}
          >
            <div className="team-table-header" style={teamTableHeaderStyle}>
              Team Member
            </div>
            <div className="team-table-header" style={teamTableHeaderStyle}>
              Major Responsibilities
            </div>

            {teamMembers.map((member, index) => (
              <div
                key={member.name}
                style={{
                  display: "contents",
                }}
              >
                <div
                  className={`team-table-cell ${index % 2 === 1 ? "team-table-cell-alt" : ""}`}
                  style={{
                    ...teamTableCellStyle,
                    borderBottom:
                      index === teamMembers.length - 1
                        ? "none"
                        : "1px solid var(--border, #dce7e5)",
                  }}
                >
                  <strong style={{ display: "block", fontWeight: 600 }}>
                    {member.name}
                  </strong>
                  {member.role && (
                    <span
                      className="muted"
                      style={{ display: "block", marginTop: "3px" }}
                    >
                      {member.role}
                    </span>
                  )}
                </div>
                <div
                  className={`team-table-cell ${index % 2 === 1 ? "team-table-cell-alt" : ""}`}
                  style={{
                    ...teamTableCellStyle,
                    borderBottom:
                      index === teamMembers.length - 1
                        ? "none"
                        : "1px solid var(--border, #dce7e5)",
                  }}
                >
                  {member.contribution}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

const teamTableHeaderStyle = {
  padding: "12px 14px",
  background: "#203b68",
  color: "#ffffff",
  fontWeight: 600,
  fontSize: "13px",
  borderRight: "1px solid rgba(255,255,255,0.25)",
};

const teamTableCellStyle = {
  padding: "15px 14px",
  fontSize: "14px",
  lineHeight: 1.5,
  color: "var(--text, #203b45)",
  borderRight: "1px solid var(--border, #dce7e5)",
};

// =========================================================
// AUTHENTICATION PAGE
// =========================================================

function AuthPage({ mode, dark, toggleTheme }) {
  const isLogin = mode === "login";

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const navigate = useNavigate();
  const location = useLocation();

  const successMessage = location.state?.successMessage;

  const handleSubmit = async (event) => {
    event.preventDefault();

    setErrorMessage("");
    setLoading(true);

    const formData = new FormData(event.currentTarget);

    const email = String(formData.get("email") || "").trim();
    const password = String(formData.get("password") || "");

    try {
      if (isLogin) {
        const response = await loginUser({
          email,
          password,
        });

        if (!response?.token) {
          throw new Error(
            "The login response did not contain an authentication token.",
          );
        }

        const user = {
          username: response.username,
          email: response.email || email,
          role: response.role || "USER",
        };

        localStorage.setItem("token", response.token);
        localStorage.setItem("user", JSON.stringify(user));

        navigate("/", { replace: true });
      } else {
        const username = String(formData.get("username") || "").trim();

        await registerUser({
          username,
          email,
          password,
        });

        navigate("/login", {
          replace: true,
          state: {
            successMessage:
              "Account created successfully. Please sign in with your email and password.",
          },
        });
      }
    } catch (error) {
      console.error("Authentication error:", error);

      const status = error.response?.status;
      const responseData = error.response?.data;

      let message =
        responseData?.message ||
        responseData?.error ||
        error.message ||
        "Something went wrong. Please try again.";

      if (status === 401) {
        message = "Invalid email or password. Please try again.";
      } else if (status === 409) {
        message =
          responseData?.message ||
          "This username or email is already registered.";
      } else if (status === 400) {
        message =
          responseData?.message || "Please check your details and try again.";
      } else if (!error.response) {
        message =
          "Cannot connect to the backend. Make sure Spring Boot is running on http://localhost:8080.";
      }

      const validationErrors = responseData?.errors;

      if (Array.isArray(validationErrors)) {
        const details = validationErrors
          .map((item) => item.defaultMessage || item.message)
          .filter(Boolean);

        if (details.length > 0) {
          message = details.join(" ");
        }
      } else if (validationErrors && typeof validationErrors === "object") {
        message = Object.values(validationErrors).join(" ");
      }

      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <header className="auth-header">
        <Brand />

        <div className="header-actions">
          <button
            className="icon-button"
            onClick={toggleTheme}
            aria-label="Toggle theme"
            type="button"
          >
            {dark ? <Sun size={19} /> : <Moon size={19} />}
          </button>

          <span className="auth-switch">
            {isLogin ? "New here?" : "Already registered?"}
          </span>

          <Link
            className="button button-outline"
            to={isLogin ? "/signup" : "/login"}
          >
            {isLogin ? "Create account" : "Sign in"}
          </Link>
        </div>
      </header>

      <main className="auth-main">
        <section className="auth-intro">
          <span className="eyebrow">
            <span className="status-dot" />
            SECURE HEALTHCARE INFRASTRUCTURE
          </span>

          <h1>
            Security you can trust.
            <br />
            <span>Keys you control.</span>
          </h1>

          <p>
            Manage cryptographic key lifecycles with protected AES-256 keys,
            controlled access and blockchain-backed integrity evidence.
          </p>

          <div className="feature-list">
            <div>
              <ShieldCheck size={19} />
              <span>Role-based access control</span>
            </div>

            <div>
              <LockKeyhole size={19} />
              <span>AES-256 key protection</span>
            </div>

            <div>
              <Blocks size={19} />
              <span>Blockchain lifecycle evidence</span>
            </div>
          </div>

          <div className="trust-strip">
            <span>
              <CheckCircle2 size={16} />
              Protected key material
            </span>

            <span>
              <CheckCircle2 size={16} />
              Auditable events
            </span>
          </div>
        </section>

        <section className="auth-card">
          <div className="auth-card-icon">
            <KeyRound size={25} />
          </div>

          <h2>{isLogin ? "Welcome back" : "Create your account"}</h2>

          <p className="muted">
            {isLogin
              ? "Sign in to access your security workspace."
              : "Register to get started with secure key management."}
          </p>

          {isLogin && successMessage && (
            <div className="auth-success" role="status">
              {successMessage}
            </div>
          )}

          {errorMessage && (
            <div className="auth-error" role="alert">
              {errorMessage}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {!isLogin && (
              <label>
                Username
                <input
                  name="username"
                  placeholder="Choose a username"
                  minLength={3}
                  maxLength={50}
                  autoComplete="username"
                  required
                  disabled={loading}
                />
              </label>
            )}

            <label>
              Email address
              <input
                type="email"
                name="email"
                placeholder="you@example.com"
                autoComplete="email"
                required
                disabled={loading}
              />
            </label>

            <label>
              Password
              <div className="password-field">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Enter your password"
                  minLength={6}
                  autoComplete={isLogin ? "current-password" : "new-password"}
                  required
                  disabled={loading}
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((value) => !value)}
                  disabled={loading}
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </label>

            <button
              className="button button-primary submit-button"
              type="submit"
              disabled={loading}
            >
              {loading
                ? isLogin
                  ? "Signing in..."
                  : "Creating account..."
                : isLogin
                  ? "Sign in securely"
                  : "Create account"}

              {!loading && <ArrowRight size={18} />}
            </button>
          </form>

          <div className="form-note">
            <LockKeyhole size={15} />
            Your credentials are handled by the secure backend.
          </div>

          <div className="auth-bottom">
            {isLogin ? "Don't have an account?" : "Already have an account?"}

            <Link to={isLogin ? "/signup" : "/login"}>
              {isLogin ? "Sign up" : "Sign in"}
            </Link>
          </div>
        </section>
      </main>

      <div className="auth-decoration decoration-one" />
      <div className="auth-decoration decoration-two" />
    </div>
  );
}

// =========================================================
// DASHBOARD
// =========================================================

function DashboardPreview({ dark, toggleTheme, onTeam }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  const navigate = useNavigate();
  const [keys, setKeys] = useState([]);
  const [keysLoading, setKeysLoading] = useState(true);
  const [keysError, setKeysError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadKeys = async () => {
      setKeysLoading(true);
      setKeysError("");

      try {
        const payload = await getKeys();
        const records = Array.isArray(payload)
          ? payload
          : Array.isArray(payload?.keys)
            ? payload.keys
            : Array.isArray(payload?.data)
              ? payload.data
              : Array.isArray(payload?.content)
                ? payload.content
                : [];

        if (!cancelled) {
          setKeys(records);
        }
      } catch (error) {
        if (cancelled) return;

        if (error.response?.status === 401) {
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          navigate("/login", { replace: true });
          return;
        }

        setKeysError(
          error.response?.data?.message ||
            error.response?.data?.error ||
            "Could not load key data. Check that the backend is running and you are signed in.",
        );
      } finally {
        if (!cancelled) {
          setKeysLoading(false);
        }
      }
    };

    loadKeys();

    return () => {
      cancelled = true;
    };
  }, [navigate]);

  const getSavedUser = () => {
    try {
      return JSON.parse(localStorage.getItem("user") || "null");
    } catch {
      return null;
    }
  };

  const savedUser = getSavedUser();

  const displayName = savedUser?.username || "User";
  const userRole = String(savedUser?.role || "USER").toUpperCase();
  const isAdmin = userRole === "ADMIN";
  const avatarLetter = displayName.charAt(0).toUpperCase();
  const hasToken = Boolean(localStorage.getItem("token"));

  const getKeyStatus = (key) =>
    String(key?.status ?? key?.keyStatus ?? "").toUpperCase();

  const totalKeys = keys.length;
  const activeKeys = keys.filter(
    (key) => getKeyStatus(key) === "ACTIVE",
  ).length;
  const revokedKeys = keys.filter(
    (key) => getKeyStatus(key) === "REVOKED",
  ).length;
  const expiredKeys = keys.filter((key) => {
    const status = getKeyStatus(key);
    if (status === "EXPIRED") return true;
    if (status === "REVOKED") return false;

    const expiration = key?.expiresAt ?? key?.expires_at;
    return expiration ? new Date(expiration).getTime() < Date.now() : false;
  }).length;

  const formatCount = (count) => (keysLoading ? "…" : String(count));

  const navItems = [
    {
      to: "/",
      label: isAdmin ? "Admin Overview" : "My Overview",
      icon: LayoutDashboard,
    },
    { to: "/keys", label: "Key Management", icon: KeyRound },
    { to: "/blockchain", label: "Blockchain Explorer", icon: Blocks },
    ...(isAdmin
      ? [
          { to: "/recovery", label: "Emergency Recovery", icon: RefreshCw },
          { to: "/audit", label: "Audit Logs", icon: Activity },
        ]
      : []),
  ];

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    setProfileOpen(false);

    navigate("/login", { replace: true });
  };

  return (
    <div className="dashboard-shell">
      <aside className={`sidebar ${menuOpen ? "sidebar-open" : ""}`}>
        <Brand />

        <div className="sidebar-label">
          {isAdmin ? "ADMINISTRATION" : "USER WORKSPACE"}
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "/"}
                onClick={() => setMenuOpen(false)}
              >
                <Icon size={19} />
                {item.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="sidebar-security">
          <div className="security-symbol">
            <ShieldCheck size={22} />
          </div>

          <strong>Security first</strong>

          <p>Keep sensitive key material protected and access controlled.</p>
        </div>

        <div className="sidebar-bottom">
          <span className="online-dot" />
          Healthcare security workspace
        </div>
      </aside>

      <div className="dashboard-main">
        <header className="dashboard-header">
          <button
            className="icon-button mobile-menu"
            onClick={() => setMenuOpen((value) => !value)}
            aria-label="Toggle navigation"
            type="button"
          >
            <Menu />
          </button>

          <div>
            <span className="muted header-kicker">
              HEALTHCARE KEY MANAGEMENT
            </span>

            <h2>{isAdmin ? "Admin workspace" : "Security workspace"}</h2>
          </div>

          <div className="header-actions">
            <button
              className="icon-button"
              onClick={toggleTheme}
              aria-label="Toggle theme"
              type="button"
            >
              {dark ? <Sun size={19} /> : <Moon size={19} />}
            </button>

            <div className="profile-wrap">
              <button
                className="profile-button"
                onClick={() => setProfileOpen((value) => !value)}
                aria-expanded={profileOpen}
                aria-label="Open account menu"
                type="button"
              >
                <span className="profile-avatar">{avatarLetter}</span>

                <span>{displayName}</span>

                <ChevronDown size={16} />
              </button>

              {profileOpen && (
                <div className="profile-menu">
                  <strong>{displayName}</strong>

                  {savedUser?.email && (
                    <span className="muted">{savedUser.email}</span>
                  )}

                  {savedUser?.role && (
                    <span className="muted">Role: {userRole}</span>
                  )}

                  <div className="profile-divider" />

                  {hasToken ? (
                    <button
                      type="button"
                      className="profile-logout"
                      onClick={handleLogout}
                    >
                      Sign out
                    </button>
                  ) : (
                    <Link to="/login" onClick={() => setProfileOpen(false)}>
                      Sign in
                    </Link>
                  )}
                </div>
              )}
            </div>
          </div>
        </header>

        <main className="dashboard-content">
          <div className="welcome-row">
            <div>
              <span className="eyebrow">
                {isAdmin ? "ADMINISTRATOR CONSOLE" : "SECURE OPERATIONS"}
              </span>

              <h1>
                {isAdmin ? "Welcome" : "Good to see you"}
                {savedUser?.username ? `, ${savedUser.username}` : ""}.
              </h1>

              <p className="muted">
                {isAdmin
                  ? "Monitor key lifecycle activity and access administrator tools."
                  : "Manage your authorized healthcare key workspace and review system status."}
              </p>
            </div>

            <span className="environment-badge">
              <span className="online-dot" />
              {isAdmin
                ? "ADMIN · Local environment"
                : "USER · Local environment"}
            </span>
          </div>

          <div className="notice-banner">
            <ShieldCheck size={22} />

            <div>
              <strong>
                {isAdmin
                  ? "Administrator access enabled"
                  : "Security is built into every layer"}
              </strong>

              <p>
                {isAdmin
                  ? "Administrator navigation is enabled for this account. Sensitive operations must also be authorized by the backend."
                  : "Your workspace uses authenticated backend APIs. Available operations depend on permissions enforced by the backend."}
              </p>
            </div>
          </div>

          {keysError && (
            <div
              className="auth-error"
              role="alert"
              style={{ marginBottom: "18px" }}
            >
              {keysError}
            </div>
          )}

          <section className="stat-grid" aria-label="Live key statistics">
            <article className="stat-card">
              <span className="stat-icon teal">
                <KeyRound />
              </span>

              <span className="muted">
                {isAdmin ? "System key records" : "Key records"}
              </span>

              <strong>{formatCount(totalKeys)}</strong>

              <small>
                {keysLoading
                  ? "Loading from backend…"
                  : isAdmin
                    ? "Backend metadata overview"
                    : "Metadata returned by /api/keys"}
              </small>
            </article>

            <article className="stat-card">
              <span className="stat-icon green">
                <ShieldCheck />
              </span>

              <span className="muted">Active keys</span>

              <strong>{formatCount(activeKeys)}</strong>

              <small>
                {keysLoading ? "Loading from backend…" : "Status: ACTIVE"}
              </small>
            </article>

            <article className="stat-card">
              <span className="stat-icon purple">
                <Blocks />
              </span>

              <span className="muted">Revoked keys</span>

              <strong>{formatCount(revokedKeys)}</strong>

              <small>
                {keysLoading ? "Loading from backend…" : "Status: REVOKED"}
              </small>
            </article>

            <article className="stat-card">
              <span className="stat-icon orange">
                <Activity />
              </span>

              <span className="muted">Expired keys</span>

              <strong>{formatCount(expiredKeys)}</strong>

              <small>
                {keysLoading
                  ? "Loading from backend…"
                  : "Expired by status or date"}
              </small>
            </article>
          </section>

          <section className="content-grid">
            <article className="panel">
              <div className="panel-heading">
                <div>
                  <h3>{isAdmin ? "System key lifecycle" : "Key lifecycle"}</h3>

                  <p className="muted">
                    Live metadata from the secured backend
                  </p>
                </div>

                <KeyRound />
              </div>

              <div className="empty-state">
                <div className="empty-icon">
                  <KeyRound size={25} />
                </div>

                <strong>
                  {keysLoading
                    ? "Loading key metadata…"
                    : keysError
                      ? "Key metadata is unavailable"
                      : totalKeys === 0
                        ? "No keys found"
                        : `${totalKeys} key record${totalKeys === 1 ? "" : "s"} loaded`}
                </strong>

                <p>
                  {keysError
                    ? "Check the backend connection and retry after signing in."
                    : keysLoading
                      ? "Fetching your authorized key metadata."
                      : totalKeys === 0
                        ? "Create a key through the key management workflow to see it here."
                        : `${activeKeys} active, ${revokedKeys} revoked and ${expiredKeys} expired.`}
                </p>

                <Link className="button button-outline" to="/keys">
                  View key management
                  <ArrowRight size={16} />
                </Link>
              </div>
            </article>

            <article className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Blockchain evidence</h3>

                  <p className="muted">Integrity and node connectivity</p>
                </div>

                <Blocks />
              </div>

              <div className="blockchain-preview">
                <span className="blockchain-orb">
                  <Blocks size={28} />
                </span>

                <strong>Verification-ready architecture</strong>

                <p>
                  Lifecycle evidence is recorded through the backend and smart
                  contract.
                </p>

                <Link to="/blockchain" className="text-link">
                  Explore blockchain
                  <ArrowRight size={15} />
                </Link>
              </div>
            </article>
          </section>

          <div className="dashboard-footer">
            <Footer onTeam={onTeam} />
          </div>
        </main>
      </div>
    </div>
  );
}

// =========================================================
// KEY MANAGEMENT PAGE — LIVE BACKEND METADATA
// =========================================================

function KeyManagementPage() {
  const [keys, setKeys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [notice, setNotice] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [refreshVersion, setRefreshVersion] = useState(0);
  const [creating, setCreating] = useState(false);
  const [busyKey, setBusyKey] = useState("");
  const [formKeyId, setFormKeyId] = useState("");
  const [formExpiresAt, setFormExpiresAt] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    async function loadKeys() {
      setLoading(true);
      setErrorMessage("");
      try {
        const response = await getKeys();
        const records = Array.isArray(response) ? response : [];
        if (!cancelled) setKeys(records);
      } catch (error) {
        if (cancelled) return;
        if (error.response?.status === 401) {
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          navigate("/login", { replace: true });
          return;
        }
        setErrorMessage(
          error.response?.data?.message ||
            error.response?.data?.error ||
            error.message ||
            "Unable to load key metadata. Check the backend connection and try again."
        );
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadKeys();
    return () => {
      cancelled = true;
    };
  }, [navigate, refreshVersion]);

  const getStatus = (key) => {
    const status = String(
      key?.status ?? key?.keyStatus ?? "UNKNOWN"
    ).toUpperCase();
    if (status === "ACTIVE" || status === "REVOKED" || status === "EXPIRED" || status === "ROTATED" || status === "ARCHIVED") {
      return status;
    }
    const expiresAt = key?.expiresAt ?? key?.expires_at;
    if (expiresAt && new Date(expiresAt).getTime() < Date.now()) return "EXPIRED";
    return status;
  };

  const formatDate = (value) => {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  };

  const getErrorText = (error, fallback) => {
    const data = error?.response?.data;
    if (typeof data === "string") return data;
    return data?.message || data?.error || error?.message || fallback;
  };

  const refreshKeys = () => setRefreshVersion((value) => value + 1);

  const handleCreate = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setNotice("");

    const keyId = formKeyId.trim();
    if (!/^[A-Za-z0-9_-]{3,100}$/.test(keyId)) {
      setErrorMessage("Key ID must be 3–100 characters and contain only letters, numbers, underscores, or hyphens.");
      return;
    }
    if (!formExpiresAt || new Date(formExpiresAt).getTime() <= Date.now()) {
      setErrorMessage("Choose an expiration date and time in the future.");
      return;
    }

    setCreating(true);
    try {
      await createKey({ keyId, expiresAt: formExpiresAt });
      setFormKeyId("");
      setFormExpiresAt("");
      setNotice(`Key "${keyId}" created successfully.`);
      refreshKeys();
    } catch (error) {
      setErrorMessage(getErrorText(error, "Could not create the key."));
    } finally {
      setCreating(false);
    }
  };

  const handleRotate = async (keyId) => {
    if (!window.confirm(`Rotate active key "${keyId}"? A new active version will be created.`)) return;
    setErrorMessage("");
    setNotice("");
    setBusyKey(`${keyId}:rotate`);
    try {
      await rotateKey(keyId);
      setNotice(`Key "${keyId}" rotated successfully.`);
      refreshKeys();
    } catch (error) {
      setErrorMessage(getErrorText(error, `Could not rotate key "${keyId}".`));
    } finally {
      setBusyKey("");
    }
  };

  const handleRevoke = async (keyId) => {
    if (!window.confirm(`Revoke active key "${keyId}"? This operation cannot be undone from this screen.`)) return;
    setErrorMessage("");
    setNotice("");
    setBusyKey(`${keyId}:revoke`);
    try {
      await revokeKey(keyId);
      setNotice(`Key "${keyId}" revoked successfully.`);
      refreshKeys();
    } catch (error) {
      setErrorMessage(getErrorText(error, `Could not revoke key "${keyId}".`));
    } finally {
      setBusyKey("");
    }
  };

  const handleArchive = async (keyId) => {
    const versions = keys.filter(
      (key) => String(key?.keyId ?? "") === keyId,
    );
    const hasActiveVersion = versions.some(
      (key) => getStatus(key) === "ACTIVE",
    );

    if (hasActiveVersion) {
      setErrorMessage(
        `Key "${keyId}" cannot be archived while an ACTIVE version exists. Rotate or revoke the active version first.`,
      );
      setNotice("");
      return;
    }

    if (!window.confirm(`Archive all eligible versions of key "${keyId}"? This action cannot be undone from this screen.`)) return;

    setErrorMessage("");
    setNotice("");
    setBusyKey(`${keyId}:archive`);
    try {
      const response = await archiveKey(keyId);
      const archivedCount =
        typeof response === "number"
          ? response
          : response?.archivedCount ?? response?.count ?? response?.archivedVersions;
      setNotice(
        Number.isFinite(Number(archivedCount))
          ? `Key "${keyId}" archived successfully (${archivedCount} version${Number(archivedCount) === 1 ? "" : "s"}).`
          : `Key "${keyId}" archived successfully.`,
      );
      refreshKeys();
    } catch (error) {
      setErrorMessage(getErrorText(error, `Could not archive key "${keyId}".`));
    } finally {
      setBusyKey("");
    }
  };

  const filteredKeys = keys.filter((key) => {
    const status = getStatus(key);
    const query = searchTerm.trim().toLowerCase();
    const matchesStatus = statusFilter === "ALL" || status === statusFilter;
    const searchable = [key?.keyId, key?.algorithm, key?.keyVersion, status]
      .map((value) => String(value ?? "").toLowerCase())
      .join(" ");
    return matchesStatus && (!query || searchable.includes(query));
  });

  const inputStyle = {
    width: "100%",
    minWidth: 0,
    padding: "11px 12px",
    border: "1px solid var(--border, #dce7e5)",
    borderRadius: "9px",
    background: "var(--surface, #fff)",
    color: "var(--text, #203b45)",
  };

  return (
    <main className="dashboard-content">
      <div className="welcome-row">
        <div>
          <span className="eyebrow">SECURE OPERATIONS</span>
          <h1>Key Management</h1>
          <p className="muted">
            Create keys and manage active key versions using the secured backend.
          </p>
        </div>
        <button
          className="button button-outline"
          type="button"
          onClick={refreshKeys}
          disabled={loading || creating || Boolean(busyKey)}
        >
          <RefreshCw size={16} /> {loading ? "Refreshing…" : "Refresh"}
        </button>
      </div>

      <div className="notice-banner">
        <ShieldCheck size={22} />
        <div>
          <strong>Metadata only</strong>
          <p>
            This screen displays key identifiers and lifecycle metadata only.
            Secret AES key material is never displayed.
          </p>
        </div>
      </div>

      {notice && (
        <div className="auth-success" role="status" style={{ marginTop: "16px" }}>
          {notice}
        </div>
      )}
      {errorMessage && (
        <div className="auth-error" role="alert" style={{ marginTop: "16px" }}>
          {errorMessage}
        </div>
      )}

      <section className="panel" style={{ marginTop: "22px" }}>
        <div className="panel-heading">
          <div>
            <h3><Plus size={18} style={{ verticalAlign: "middle", marginRight: "8px" }} />Create AES-256 key</h3>
            <p className="muted">Create a new key record with a future expiration date.</p>
          </div>
          <KeyRound />
        </div>
        <form
          onSubmit={handleCreate}
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
            gap: "14px",
            alignItems: "end",
            marginTop: "16px",
          }}
        >
          <label>
            Key ID
            <input
              style={inputStyle}
              value={formKeyId}
              onChange={(event) => setFormKeyId(event.target.value)}
              placeholder="e.g. PATIENT-DATA-001"
              minLength={3}
              maxLength={100}
              pattern="[A-Za-z0-9_-]+"
              title="Use 3–100 letters, numbers, underscores, or hyphens."
              required
              disabled={creating}
            />
          </label>
          <label>
            Expires at
            <input
              style={inputStyle}
              type="datetime-local"
              value={formExpiresAt}
              onChange={(event) => setFormExpiresAt(event.target.value)}
              required
              disabled={creating}
            />
          </label>
          <button className="button button-primary" type="submit" disabled={creating}>
            <Plus size={16} /> {creating ? "Creating…" : "Create key"}
          </button>
        </form>
        <p className="muted" style={{ marginTop: "12px", fontSize: "12px" }}>
          Algorithm is set to AES-256 by the frontend. The backend validates the request and generates/protects the key material.
        </p>
      </section>

      <section className="panel" style={{ marginTop: "22px" }}>
        <div className="panel-heading" style={{ alignItems: "center", flexWrap: "wrap" }}>
          <div>
            <h3>Key records</h3>
            <p className="muted">
              {loading ? "Loading from /api/keys…" : `${filteredKeys.length} of ${keys.length} records`}
            </p>
          </div>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            <label style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <Search size={16} aria-hidden="true" />
              <input
                type="search"
                aria-label="Search key records"
                placeholder="Search key ID or algorithm"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                style={{ ...inputStyle, minWidth: "220px", maxWidth: "100%" }}
              />
            </label>
            <select
              aria-label="Filter keys by status"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              style={inputStyle}
            >
              <option value="ALL">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="ROTATED">Rotated</option>
              <option value="REVOKED">Revoked</option>
              <option value="EXPIRED">Expired</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>
        </div>

        {!loading && !errorMessage && keys.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon"><KeyRound size={25} /></div>
            <strong>No key records found</strong>
            <p>Create your first key using the form above.</p>
          </div>
        )}

        {!loading && !errorMessage && keys.length > 0 && filteredKeys.length === 0 && (
          <div className="empty-state">
            <strong>No matching records</strong>
            <p>Try a different search term or status filter.</p>
          </div>
        )}

        {(loading || (!errorMessage && filteredKeys.length > 0)) && (
          <div style={{ width: "100%", overflowX: "auto", marginTop: "16px" }}>
            <table style={{ width: "100%", borderCollapse: "collapse", minWidth: "1000px" }}>
              <thead>
                <tr>
                  {["Key ID", "Algorithm", "Version", "Status", "Created at", "Expires at", "Actions"].map((label) => (
                    <th key={label} style={{
                      textAlign: "left",
                      padding: "13px 14px",
                      background: "#203b68",
                      color: "#fff",
                      fontSize: "13px",
                      fontWeight: 600,
                      whiteSpace: "nowrap",
                    }}>
                      {label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan={7} style={{ padding: "24px 14px", textAlign: "center" }}>Loading key metadata…</td></tr>
                ) : (
                  filteredKeys.map((key, index) => {
                    const status = getStatus(key);
                    const keyId = String(key?.keyId ?? "");
                    const rowBusy = busyKey.startsWith(`${keyId}:`);
                    const badgeColors =
                      status === "ACTIVE"
                        ? { background: "#dcfce7", color: "#166534" }
                        : status === "REVOKED"
                          ? { background: "#fee2e2", color: "#991b1b" }
                          : status === "EXPIRED"
                            ? { background: "#ffedd5", color: "#9a3412" }
                            : { background: "var(--surface-soft, #edf3f6)", color: "var(--text, #203b45)" };
                    const cellStyle = { padding: "14px", borderBottom: "1px solid var(--border, #dce7e5)", whiteSpace: "nowrap" };
                    return (
                      <tr key={key.id ?? `${keyId || "key"}-${key.keyVersion ?? index}`}>
                        <td style={{ ...cellStyle, fontWeight: 600 }}>{keyId || "—"}</td>
                        <td style={cellStyle}>{key.algorithm || "—"}</td>
                        <td style={cellStyle}>{key.keyVersion ?? "—"}</td>
                        <td style={cellStyle}>
                          <span style={{ display: "inline-block", borderRadius: "999px", padding: "5px 9px", fontSize: "12px", fontWeight: 700, ...badgeColors }}>
                            {status}
                          </span>
                        </td>
                        <td style={cellStyle}>{formatDate(key.createdAt ?? key.created_at)}</td>
                        <td style={cellStyle}>{formatDate(key.expiresAt ?? key.expires_at)}</td>
                        <td style={cellStyle}>
                          {status === "ACTIVE" ? (
                            <div style={{ display: "flex", gap: "8px" }}>
                              <button
                                type="button"
                                className="button button-outline"
                                style={{ padding: "8px 10px", fontSize: "12px" }}
                                disabled={Boolean(busyKey) || creating}
                                onClick={() => handleRotate(keyId)}
                                title="Create a new active version"
                              >
                                <RotateCw size={14} /> {busyKey === `${keyId}:rotate` ? "Rotating…" : "Rotate"}
                              </button>
                              <button
                                type="button"
                                className="button button-outline"
                                style={{ padding: "8px 10px", fontSize: "12px" }}
                                disabled={Boolean(busyKey) || creating}
                                onClick={() => handleRevoke(keyId)}
                                title="Revoke the active version"
                              >
                                <Ban size={14} /> {busyKey === `${keyId}:revoke` ? "Revoking…" : "Revoke"}
                              </button>
                            </div>
                          ) : status === "ARCHIVED" ? (
                            <span className="muted" style={{ fontSize: "12px" }}>Already archived</span>
                          ) : ["ROTATED", "REVOKED", "EXPIRED"].includes(status) &&
                            !keys.some(
                              (item) =>
                                String(item?.keyId ?? "") === keyId &&
                                getStatus(item) === "ACTIVE",
                            ) ? (
                            <button
                              type="button"
                              className="button button-outline"
                              style={{ padding: "8px 10px", fontSize: "12px" }}
                              disabled={Boolean(busyKey) || creating}
                              onClick={() => handleArchive(keyId)}
                              title="Archive eligible versions of this key"
                            >
                              <LockKeyhole size={14} /> {busyKey === `${keyId}:archive` ? "Archiving…" : "Archive"}
                            </button>
                          ) : (
                            <span className="muted" style={{ fontSize: "12px" }}>
                              { ["ROTATED", "REVOKED", "EXPIRED"].includes(status)
                                ? "Revoke active version before archiving"
                                : "No actions available"}
                            </span>
                          )}
                          {rowBusy && <span className="muted" style={{ display: "block", marginTop: "4px", fontSize: "11px" }}>Updating…</span>}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

// =========================================================
// AUDIT LOGS PAGE — ADMIN-ONLY BACKEND DATA
// =========================================================

function AuditLogsPage() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [refreshVersion, setRefreshVersion] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    async function loadAuditLogs() {
      setLoading(true);
      setErrorMessage("");
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          navigate("/login", { replace: true });
          return;
        }

        const response = await fetch(
          "http://localhost:8080/api/admin/audit-logs",
          {
            method: "GET",
            headers: {
              Accept: "application/json",
              Authorization: `Bearer ${token}`,
            },
          },
        );

        if (response.status === 401) {
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          navigate("/login", { replace: true });
          return;
        }
        if (response.status === 403) {
          throw new Error(
            "Access denied. An administrator account is required to view audit logs.",
          );
        }
        if (!response.ok) {
          let detail = "";
          try {
            const body = await response.json();
            detail = body?.message || body?.error || "";
          } catch {
            /* The server may return a non-JSON error body. */
          }
          throw new Error(
            detail || `Unable to load audit logs (HTTP ${response.status}).`,
          );
        }

        const payload = await response.json();
        const records = Array.isArray(payload)
          ? payload
          : Array.isArray(payload?.content)
            ? payload.content
            : Array.isArray(payload?.data)
              ? payload.data
              : Array.isArray(payload?.logs)
                ? payload.logs
                : [];
        if (!cancelled) setLogs(records);
      } catch (error) {
        if (!cancelled)
          setErrorMessage(
            error.message ||
              "Unable to load audit logs. Check the backend connection and try again.",
          );
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadAuditLogs();
    return () => {
      cancelled = true;
    };
  }, [navigate, refreshVersion]);

  const formatDate = (value) => {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
  };

  const getStatus = (log) => {
    const value = Number(log?.status);
    if (!Number.isFinite(value)) return "UNKNOWN";
    return value >= 200 && value < 400 ? "SUCCESS" : "FAILURE";
  };

  const filteredLogs = logs.filter((log) => {
    const status = getStatus(log);
    const matchesStatus = statusFilter === "ALL" || status === statusFilter;
    const query = searchTerm.trim().toLowerCase();
    const searchable = [
      log?.username,
      log?.action,
      log?.method,
      log?.endpoint,
      log?.status,
      log?.timestamp,
    ]
      .map((value) => String(value ?? "").toLowerCase())
      .join(" ");
    return matchesStatus && (!query || searchable.includes(query));
  });

  const successCount = logs.filter(
    (log) => getStatus(log) === "SUCCESS",
  ).length;
  const failureCount = logs.filter(
    (log) => getStatus(log) === "FAILURE",
  ).length;

  return (
    <main className="dashboard-content">
      <div className="welcome-row">
        <div>
          <span className="eyebrow">
            <Activity size={14} /> ADMINISTRATION
          </span>
          <h1>Audit Logs</h1>
          <p className="muted">
            Review recorded API activity and response status.
          </p>
        </div>
        <button
          className="button button-outline"
          type="button"
          onClick={() => setRefreshVersion((value) => value + 1)}
          disabled={loading}
        >
          <RefreshCw size={16} /> {loading ? "Refreshing…" : "Refresh logs"}
        </button>
      </div>

      <div className="notice-banner">
        <ShieldCheck size={22} />
        <div>
          <strong>Administrator-only audit view</strong>
          <p>
            Records are retrieved from the secured backend endpoint. This page
            displays audit metadata, not cryptographic key material.
          </p>
        </div>
      </div>

      <section className="audit-summary-grid" aria-label="Audit log summary">
        <article className="stat-card">
          <span className="stat-icon teal">
            <Activity />
          </span>
          <span className="muted">Total records</span>
          <strong>{loading ? "…" : logs.length}</strong>
          <small>Records returned by the backend</small>
        </article>
        <article className="stat-card">
          <span className="stat-icon green">
            <ShieldCheck />
          </span>
          <span className="muted">Successful responses</span>
          <strong>{loading ? "…" : successCount}</strong>
          <small>HTTP 2xx and 3xx</small>
        </article>
        <article className="stat-card">
          <span className="stat-icon orange">
            <Activity />
          </span>
          <span className="muted">Failed responses</span>
          <strong>{loading ? "…" : failureCount}</strong>
          <small>HTTP 4xx and 5xx</small>
        </article>
      </section>

      <section className="panel audit-panel">
        <div className="panel-heading audit-toolbar">
          <div>
            <h3>Recorded activity</h3>
            <p className="muted">
              {loading
                ? "Loading from /api/admin/audit-logs…"
                : `${filteredLogs.length} of ${logs.length} records`}
            </p>
          </div>
          <div className="audit-filters">
            <label className="audit-search">
              <Search size={16} aria-hidden="true" />
              <input
                type="search"
                aria-label="Search audit logs"
                placeholder="Search username, action, endpoint…"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
            </label>
            <select
              aria-label="Filter audit logs by result"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="ALL">All results</option>
              <option value="SUCCESS">Successful</option>
              <option value="FAILURE">Failed</option>
              <option value="UNKNOWN">Unknown status</option>
            </select>
          </div>
        </div>

        {errorMessage && (
          <div
            className="auth-error"
            role="alert"
            style={{ marginTop: "16px" }}
          >
            {errorMessage}
          </div>
        )}
        {!loading && !errorMessage && logs.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">
              <Activity size={25} />
            </div>
            <strong>No audit records found</strong>
            <p>
              The endpoint returned an empty list. Perform an API action and
              refresh this page.
            </p>
          </div>
        )}
        {!loading &&
          !errorMessage &&
          logs.length > 0 &&
          filteredLogs.length === 0 && (
            <div className="empty-state">
              <strong>No matching audit records</strong>
              <p>Try a different search term or result filter.</p>
            </div>
          )}

        {(loading || (!errorMessage && filteredLogs.length > 0)) && (
          <div className="audit-table-wrap">
            <table className="audit-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Action</th>
                  <th>Method</th>
                  <th>Endpoint</th>
                  <th>HTTP status</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={7} className="audit-empty-cell">
                      Loading audit records…
                    </td>
                  </tr>
                ) : (
                  filteredLogs.map((log, index) => {
                    const result = getStatus(log);
                    const statusCode = Number(log?.status);
                    return (
                      <tr
                        key={log?.id ?? `${log?.timestamp ?? "audit"}-${index}`}
                      >
                        <td>{log?.id ?? "—"}</td>
                        <td className="audit-username">
                          {log?.username || "—"}
                        </td>
                        <td>{log?.action || "—"}</td>
                        <td>
                          <span className="audit-method">
                            {log?.method || "—"}
                          </span>
                        </td>
                        <td className="audit-endpoint">
                          {log?.endpoint || "—"}
                        </td>
                        <td>
                          <span
                            className={`audit-status ${result.toLowerCase()}`}
                          >
                            {Number.isFinite(statusCode)
                              ? statusCode
                              : "Unknown"}
                          </span>
                        </td>
                        <td className="audit-timestamp">
                          {formatDate(log?.timestamp)}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

// =========================================================
// PLACEHOLDER PAGES
// =========================================================

function PlaceholderPage({ title, description }) {
  return (
    <div className="placeholder-page">
      <div className="empty-icon">
        <KeyRound size={26} />
      </div>

      <span className="eyebrow">WORKSPACE MODULE</span>

      <h1>{title}</h1>

      <p>{description}</p>

      <span className="environment-badge">
        Integration planned for the next implementation steps
      </span>

      <Link className="button button-outline" to="/">
        Back to overview
      </Link>
    </div>
  );
}

// =========================================================
// ROUTE GUARDS
// =========================================================

// Frontend navigation guard. Backend APIs must still validate the JWT.
function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");

  if (!token) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: window.location.pathname }}
      />
    );
  }

  return children;
}

// Frontend role guard for admin-only screens. Backend authorization remains mandatory.
function AdminRoute({ children }) {
  const token = localStorage.getItem("token");
  let user = null;

  try {
    user = JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    user = null;
  }

  if (!token) return <Navigate to="/login" replace />;

  if (String(user?.role || "").toUpperCase() !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return children;
}

// Keep users with a saved token out of login/signup pages.
function PublicOnlyRoute({ children }) {
  const token = localStorage.getItem("token");

  if (token) {
    return <Navigate to="/" replace />;
  }

  return children;
}

// =========================================================
// MAIN APP
// =========================================================

function App() {
  const [dark, setDark] = useState(false);
  const [teamOpen, setTeamOpen] = useState(false);

  const toggleTheme = () => {
    setDark((value) => !value);
  };

  return (
    <div className={dark ? "app theme-dark" : "app theme-light"}>
      <BrowserRouter>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicOnlyRoute>
                <AuthPage mode="login" dark={dark} toggleTheme={toggleTheme} />
              </PublicOnlyRoute>
            }
          />

          <Route
            path="/signup"
            element={
              <PublicOnlyRoute>
                <AuthPage mode="signup" dark={dark} toggleTheme={toggleTheme} />
              </PublicOnlyRoute>
            }
          />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <DashboardPreview
                  dark={dark}
                  toggleTheme={toggleTheme}
                  onTeam={() => setTeamOpen(true)}
                />
              </ProtectedRoute>
            }
          />

          <Route
            path="/keys"
            element={
              <ProtectedRoute>
                <KeyManagementPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/blockchain"
            element={
              <ProtectedRoute>
                <BlockchainExplorer />
              </ProtectedRoute>
            }
          />

          <Route
            path="/recovery"
            element={
              <AdminRoute>
                <KeyRecoveryPanel role="ADMIN" />
              </AdminRoute>
            }
          />

          <Route
            path="/audit"
            element={
              <AdminRoute>
                <AuditLogsPage />
              </AdminRoute>
            }
          />

          <Route
            path="*"
            element={
              <PlaceholderPage
                title="Page not found"
                description="The requested workspace page does not exist."
              />
            }
          />
        </Routes>

        {teamOpen && <TeamModal onClose={() => setTeamOpen(false)} />}
      </BrowserRouter>
    </div>
  );
}

export default App;
