import { useState } from 'react'
import {
  BrowserRouter,
  Link,
  NavLink,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useLocation,
} from 'react-router-dom'
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
  ShieldCheck,
  Sun,
  X,
} from 'lucide-react'
import { loginUser, registerUser } from './api/authApi'
import './App.css'

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
  )
}

// =========================================================
// FOOTER
// =========================================================


function Footer({ onTeam }) {
  return (
    <footer className="footer">
      <span>
        Copyright © 2026 Nicode2707. All rights reserved.
      </span>

      <button
        type="button"
        className="footer-link"
        onClick={onTeam}
      >
        Project Team
      </button>
    </footer>
  )
}



// =========================================================
// PROJECT TEAM MODAL
// =========================================================

function TeamModal({ onClose }) {
  const teamMembers = [
    {
      name: 'Niraj Kumar',
      role: 'Team Leader',
      contribution: 'Overall system architecture and backend development',
    },
    {
      name: 'Piyush Kumar',
      contribution: 'Authentication & Authorization',
    },
    {
      name: 'Kanhaiya Kumar',
      contribution: 'Database Design & Handling',
    },
    {
      name: 'Prashant Kumar',
      contribution: 'Blockchain Integration',
    },
  ]

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        className="team-modal"
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="team-modal-title"
        style={{ maxWidth: '760px', maxHeight: '90vh', overflowY: 'auto' }}
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
          Blockchain-Assisted Distributed AES Key Lifecycle Management
          Framework for Secure Healthcare IoT.
        </p>

        <div className="team-note">
          <ShieldCheck size={20} />

          <p>
            Designed to support secure cryptographic key lifecycle
            management, controlled access and blockchain-backed integrity
            evidence for healthcare systems.
          </p>
        </div>

        <div style={{ marginTop: '28px' }}>
          <span className="eyebrow">PROJECT TEAM MEMBERS</span>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'minmax(130px, 0.9fr) minmax(0, 1.5fr)',
              marginTop: '14px',
              border: '1px solid var(--border, #dce7e5)',
              borderRadius: '10px',
              overflow: 'hidden',
            }}
          >
            <div className="team-table-header" style={teamTableHeaderStyle}>Team Member</div>
            <div className="team-table-header" style={teamTableHeaderStyle}>Major Responsibilities</div>

            {teamMembers.map((member, index) => (
              <div
                key={member.name}
                style={{
                  display: 'contents',
                }}
              >
                <div
                  className={`team-table-cell ${index % 2 === 1 ? 'team-table-cell-alt' : ''}`}
                  style={{
                    ...teamTableCellStyle,
                    borderBottom: index === teamMembers.length - 1 ? 'none' : '1px solid var(--border, #dce7e5)',
                  }}
                >
                  <strong style={{ display: 'block', fontWeight: 600 }}>
                    {member.name}
                  </strong>
                  {member.role && (
                    <span className="muted" style={{ display: 'block', marginTop: '3px' }}>
                      {member.role}
                    </span>
                  )}
                </div>
                <div
                  className={`team-table-cell ${index % 2 === 1 ? 'team-table-cell-alt' : ''}`}
                  style={{
                    ...teamTableCellStyle,
                    borderBottom: index === teamMembers.length - 1 ? 'none' : '1px solid var(--border, #dce7e5)',
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
  )
}

const teamTableHeaderStyle = {
  padding: '12px 14px',
  background: '#203b68',
  color: '#ffffff',
  fontWeight: 600,
  fontSize: '13px',
  borderRight: '1px solid rgba(255,255,255,0.25)',
}

const teamTableCellStyle = {
  padding: '15px 14px',
  fontSize: '14px',
  lineHeight: 1.5,
  color: 'var(--text, #203b45)',
  borderRight: '1px solid var(--border, #dce7e5)',
}


// =========================================================
// AUTHENTICATION PAGE
// =========================================================

function AuthPage({ mode, dark, toggleTheme }) {
  const isLogin = mode === 'login'

  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const navigate = useNavigate()
  const location = useLocation()

  const successMessage = location.state?.successMessage

  const handleSubmit = async (event) => {
    event.preventDefault()

    setErrorMessage('')
    setLoading(true)

    const formData = new FormData(event.currentTarget)

    const email = String(formData.get('email') || '').trim()
    const password = String(formData.get('password') || '')

    try {
      if (isLogin) {
        const response = await loginUser({
          email,
          password,
        })

        if (!response?.token) {
          throw new Error(
            'The login response did not contain an authentication token.'
          )
        }

        const user = {
          username: response.username,
          email: response.email || email,
          role: response.role || 'USER',
        }

        localStorage.setItem('token', response.token)
        localStorage.setItem('user', JSON.stringify(user))

        navigate('/', { replace: true })
      } else {
        const username = String(
          formData.get('username') || ''
        ).trim()

        await registerUser({
          username,
          email,
          password,
        })

        navigate('/login', {
          replace: true,
          state: {
            successMessage:
              'Account created successfully. Please sign in with your email and password.',
          },
        })
      }
    } catch (error) {
      console.error('Authentication error:', error)

      const status = error.response?.status
      const responseData = error.response?.data

      let message =
        responseData?.message ||
        responseData?.error ||
        error.message ||
        'Something went wrong. Please try again.'

      if (status === 401) {
        message = 'Invalid email or password. Please try again.'
      } else if (status === 409) {
        message =
          responseData?.message ||
          'This username or email is already registered.'
      } else if (status === 400) {
        message =
          responseData?.message ||
          'Please check your details and try again.'
      } else if (!error.response) {
        message =
          'Cannot connect to the backend. Make sure Spring Boot is running on http://localhost:8080.'
      }

      const validationErrors = responseData?.errors

      if (Array.isArray(validationErrors)) {
        const details = validationErrors
          .map((item) => item.defaultMessage || item.message)
          .filter(Boolean)

        if (details.length > 0) {
          message = details.join(' ')
        }
      } else if (
        validationErrors &&
        typeof validationErrors === 'object'
      ) {
        message = Object.values(validationErrors).join(' ')
      }

      setErrorMessage(message)
    } finally {
      setLoading(false)
    }
  }

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
            {isLogin ? 'New here?' : 'Already registered?'}
          </span>

          <Link
            className="button button-outline"
            to={isLogin ? '/signup' : '/login'}
          >
            {isLogin ? 'Create account' : 'Sign in'}
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

          <h2>
            {isLogin ? 'Welcome back' : 'Create your account'}
          </h2>

          <p className="muted">
            {isLogin
              ? 'Sign in to access your security workspace.'
              : 'Register to get started with secure key management.'}
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
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  placeholder="Enter your password"
                  minLength={6}
                  autoComplete={
                    isLogin ? 'current-password' : 'new-password'
                  }
                  required
                  disabled={loading}
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowPassword((value) => !value)
                  }
                  disabled={loading}
                >
                  {showPassword ? 'Hide' : 'Show'}
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
                  ? 'Signing in...'
                  : 'Creating account...'
                : isLogin
                  ? 'Sign in securely'
                  : 'Create account'}

              {!loading && <ArrowRight size={18} />}
            </button>
          </form>

          <div className="form-note">
            <LockKeyhole size={15} />
            Your credentials are handled by the secure backend.
          </div>

          <div className="auth-bottom">
            {isLogin
              ? "Don't have an account?"
              : 'Already have an account?'}

            <Link to={isLogin ? '/signup' : '/login'}>
              {isLogin ? 'Sign up' : 'Sign in'}
            </Link>
          </div>
        </section>
      </main>

      <div className="auth-decoration decoration-one" />
      <div className="auth-decoration decoration-two" />
    </div>
  )
}

// =========================================================
// DASHBOARD
// =========================================================

function DashboardPreview({ dark, toggleTheme, onTeam }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)

  const navigate = useNavigate()

  const getSavedUser = () => {
    try {
      return JSON.parse(localStorage.getItem('user') || 'null')
    } catch {
      return null
    }
  }

  const savedUser = getSavedUser()

  const displayName = savedUser?.username || 'User'
  const avatarLetter = displayName.charAt(0).toUpperCase()
  const hasToken = Boolean(localStorage.getItem('token'))

  const navItems = [
    {
      to: '/',
      label: 'Overview',
      icon: LayoutDashboard,
    },
    {
      to: '/keys',
      label: 'Key Management',
      icon: KeyRound,
    },
    {
      to: '/blockchain',
      label: 'Blockchain Explorer',
      icon: Blocks,
    },
    {
      to: '/audit',
      label: 'Audit Logs',
      icon: Activity,
    },
  ]

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')

    setProfileOpen(false)

    navigate('/login', { replace: true })
  }

  return (
    <div className="dashboard-shell">
      <aside
        className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}
      >
        <Brand />

        <div className="sidebar-label">WORKSPACE</div>

        <nav className="sidebar-nav">
          {navItems.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                onClick={() => setMenuOpen(false)}
              >
                <Icon size={19} />
                {item.label}
              </NavLink>
            )
          })}
        </nav>

        <div className="sidebar-security">
          <div className="security-symbol">
            <ShieldCheck size={22} />
          </div>

          <strong>Security first</strong>

          <p>
            Keep sensitive key material protected and access controlled.
          </p>
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

            <h2>Security workspace</h2>
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
                onClick={() =>
                  setProfileOpen((value) => !value)
                }
                aria-expanded={profileOpen}
                aria-label="Open account menu"
                type="button"
              >
                <span className="profile-avatar">
                  {avatarLetter}
                </span>

                <span>{displayName}</span>

                <ChevronDown size={16} />
              </button>

              {profileOpen && (
                <div className="profile-menu">
                  <strong>{displayName}</strong>

                  {savedUser?.email && (
                    <span className="muted">
                      {savedUser.email}
                    </span>
                  )}

                  {savedUser?.role && (
                    <span className="muted">
                      Role: {savedUser.role}
                    </span>
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
                    <Link
                      to="/login"
                      onClick={() => setProfileOpen(false)}
                    >
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
              <span className="eyebrow">SECURE OPERATIONS</span>

              <h1>
                Good to see you
                {savedUser?.username
                  ? `, ${savedUser.username}`
                  : ''}
                .
              </h1>

              <p className="muted">
                Your healthcare key security workspace.
              </p>
            </div>

            <span className="environment-badge">
              <span className="online-dot" />
              Local environment
            </span>
          </div>

          <div className="notice-banner">
            <ShieldCheck size={22} />

            <div>
              <strong>Security is built into every layer</strong>

              <p>
                Authentication is connected. Key lifecycle and blockchain
                dashboard values will be populated as their API integration
                is completed.
              </p>
            </div>
          </div>

          <section className="stat-grid">
            <article className="stat-card">
              <span className="stat-icon teal">
                <KeyRound />
              </span>

              <span className="muted">Managed key versions</span>

              <strong>—</strong>

              <small>Awaiting live API connection</small>
            </article>

            <article className="stat-card">
              <span className="stat-icon green">
                <ShieldCheck />
              </span>

              <span className="muted">Active keys</span>

              <strong>—</strong>

              <small>Awaiting live API connection</small>
            </article>

            <article className="stat-card">
              <span className="stat-icon purple">
                <Blocks />
              </span>

              <span className="muted">Blockchain status</span>

              <strong>Not checked</strong>

              <small>Will use the backend connection endpoint</small>
            </article>

            <article className="stat-card">
              <span className="stat-icon orange">
                <Activity />
              </span>

              <span className="muted">Audit events</span>

              <strong>—</strong>

              <small>Awaiting live API connection</small>
            </article>
          </section>

          <section className="content-grid">
            <article className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Key lifecycle</h3>

                  <p className="muted">
                    Available lifecycle operations
                  </p>
                </div>

                <KeyRound />
              </div>

              <div className="empty-state">
                <div className="empty-icon">
                  <KeyRound size={25} />
                </div>

                <strong>No live key data connected yet</strong>

                <p>
                  Connect to the backend to view and manage key metadata.
                </p>

                <Link
                  className="button button-outline"
                  to="/keys"
                >
                  View key management
                  <ArrowRight size={16} />
                </Link>
              </div>
            </article>

            <article className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Blockchain evidence</h3>

                  <p className="muted">
                    Integrity and node connectivity
                  </p>
                </div>

                <Blocks />
              </div>

              <div className="blockchain-preview">
                <span className="blockchain-orb">
                  <Blocks size={28} />
                </span>

                <strong>Verification-ready architecture</strong>

                <p>
                  Lifecycle evidence is recorded through the backend
                  and smart contract.
                </p>

                <Link
                  to="/blockchain"
                  className="text-link"
                >
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
  )
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
  )
}


// =========================================================
// ROUTE GUARDS
// =========================================================

// Frontend navigation guard. Backend APIs must still validate the JWT.
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token')

  if (!token) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: window.location.pathname }}
      />
    )
  }

  return children
}

// Keep users with a saved token out of login/signup pages.
function PublicOnlyRoute({ children }) {
  const token = localStorage.getItem('token')

  if (token) {
    return <Navigate to="/" replace />
  }

  return children
}

// =========================================================
// MAIN APP
// =========================================================

function App() {
  const [dark, setDark] = useState(false)
  const [teamOpen, setTeamOpen] = useState(false)

  const toggleTheme = () => {
    setDark((value) => !value)
  }

  return (
    <div className={dark ? 'app theme-dark' : 'app theme-light'}>
      <BrowserRouter>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicOnlyRoute>
                <AuthPage
                  mode="login"
                  dark={dark}
                  toggleTheme={toggleTheme}
                />
              </PublicOnlyRoute>
            }
          />

          <Route
            path="/signup"
            element={
              <PublicOnlyRoute>
                <AuthPage
                  mode="signup"
                  dark={dark}
                  toggleTheme={toggleTheme}
                />
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
                <PlaceholderPage
                  title="Key Management"
                  description="Create, rotate, revoke and inspect key metadata using the Spring Boot API."
                />
              </ProtectedRoute>
            }
          />

          <Route
            path="/blockchain"
            element={
              <ProtectedRoute>
                <PlaceholderPage
                  title="Blockchain Explorer"
                  description="Inspect node connectivity and lifecycle integrity evidence through backend endpoints."
                />
              </ProtectedRoute>
            }
          />

          <Route
            path="/audit"
            element={
              <ProtectedRoute>
                <PlaceholderPage
                  title="Audit Logs"
                  description="Review authorized audit events from the backend."
                />
              </ProtectedRoute>
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

        {teamOpen && (
          <TeamModal onClose={() => setTeamOpen(false)} />
        )}
      </BrowserRouter>
    </div>
  )
}

export default App

