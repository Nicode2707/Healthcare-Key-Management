import { useState } from 'react'
import { BrowserRouter, Link, NavLink, Route, Routes } from 'react-router-dom'
import {
  Activity, ArrowRight, Blocks, CheckCircle2, ChevronDown,
  CircleUserRound, KeyRound, LayoutDashboard, LockKeyhole,
  Menu, Moon, ShieldCheck, Sun, Users, X
} from 'lucide-react'
import './App.css'

function Brand() {
  return (
    <Link to="/" className="brand">
      <span className="brand-icon"><ShieldCheck size={23} /></span>
      <span>Nicode<span className="brand-accent">2707</span></span>
    </Link>
  )
}

function Footer({ onTeam }) {
  return (
    <footer className="footer">
      <span>Copyright © 2026 <strong>Nicode2707</strong></span>
      <button className="footer-link" onClick={onTeam}>Team</button>
    </footer>
  )
}

function TeamModal({ onClose }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section className="team-modal" onClick={event => event.stopPropagation()}>
        <button className="icon-button modal-close" onClick={onClose} aria-label="Close team modal">
          <X size={20} />
        </button>
        <span className="eyebrow">THE PEOPLE BEHIND THE PROJECT</span>
        <h2>Our Project Team</h2>
        <p className="muted">Healthcare Key Management</p>
        <div className="team-card">
          <div className="avatar">NK</div>
          <div><strong>Niraj Kumar</strong><p>Team Leader · Architecture & Backend</p></div>
          <span className="member-tag">Lead</span>
        </div>
        <div className="team-card">
          <div className="avatar avatar-green">PK</div>
          <div><strong>Piyush Kumar</strong><p>Authentication & Authorization</p></div>
          <span className="member-tag">Security</span>
        </div>
        <div className="team-card">
          <div className="avatar avatar-orange">KK</div>
          <div><strong>Kanhaiya Kumar</strong><p>Database Design & Handling</p></div>
          <span className="member-tag">Database</span>
        </div>
        <div className="team-card">
          <div className="avatar avatar-purple">PK</div>
          <div><strong>Prashant Kumar</strong><p>Blockchain Integration</p></div>
          <span className="member-tag">Blockchain</span>
        </div>
        <p className="team-note">Contribution details can be updated with the team's final approved descriptions.</p>
      </section>
    </div>
  )
}

function AuthPage({ mode, dark, toggleTheme }) {
  const isLogin = mode === 'login'
  const [showPassword, setShowPassword] = useState(false)

  return (
    <div className="auth-shell">
      <header className="auth-header">
        <Brand />
        <div className="header-actions">
          <button className="icon-button" onClick={toggleTheme} aria-label="Toggle theme">
            {dark ? <Sun size={19} /> : <Moon size={19} />}
          </button>
          <span className="auth-switch">
            {isLogin ? 'New here?' : 'Already registered?'}
          </span>
          <Link className="button button-outline" to={isLogin ? '/signup' : '/login'}>
            {isLogin ? 'Create account' : 'Sign in'}
          </Link>
        </div>
      </header>

      <main className="auth-main">
        <section className="auth-intro">
          <span className="eyebrow"><span className="status-dot" /> SECURE HEALTHCARE INFRASTRUCTURE</span>
          <h1>Security you can trust.<br /><span>Keys you control.</span></h1>
          <p>Manage cryptographic key lifecycles with protected AES-256 keys, controlled access and blockchain-backed integrity evidence.</p>
          <div className="feature-list">
            <div><ShieldCheck size={19} /><span>Role-based access control</span></div>
            <div><LockKeyhole size={19} /><span>AES-256 key protection</span></div>
            <div><Blocks size={19} /><span>Blockchain lifecycle evidence</span></div>
          </div>
          <div className="trust-strip">
            <span><CheckCircle2 size={16} /> Protected key material</span>
            <span><CheckCircle2 size={16} /> Auditable events</span>
          </div>
        </section>

        <section className="auth-card">
          <div className="auth-card-icon"><KeyRound size={25} /></div>
          <h2>{isLogin ? 'Welcome back' : 'Create your account'}</h2>
          <p className="muted">{isLogin ? 'Sign in to access your security workspace.' : 'Register to get started with secure key management.'}</p>

          <form onSubmit={event => { event.preventDefault(); alert('Authentication API integration is scheduled for Day 2.') }}>
            {!isLogin && (
              <label>Username
                <input name="username" placeholder="Choose a username" minLength={3} maxLength={50} required />
              </label>
            )}
            <label>Email address
              <input type="email" name="email" placeholder="you@example.com" autoComplete="email" required />
            </label>
            <label>Password
              <div className="password-field">
                <input type={showPassword ? 'text' : 'password'} name="password" placeholder="Enter your password" minLength={6} autoComplete={isLogin ? 'current-password' : 'new-password'} required />
                <button type="button" className="password-toggle" onClick={() => setShowPassword(value => !value)}>{showPassword ? 'Hide' : 'Show'}</button>
              </div>
            </label>
            <button className="button button-primary submit-button" type="submit">
              {isLogin ? 'Sign in securely' : 'Create account'} <ArrowRight size={18} />
            </button>
          </form>

          <div className="form-note"><LockKeyhole size={15} /> Your credentials are handled by the secure backend.</div>
          <div className="auth-bottom">
            {isLogin ? "Don't have an account?" : 'Already have an account?'}
            <Link to={isLogin ? '/signup' : '/login'}>{isLogin ? 'Sign up' : 'Sign in'}</Link>
          </div>
        </section>
      </main>
      <div className="auth-decoration decoration-one" />
      <div className="auth-decoration decoration-two" />
    </div>
  )
}

function DashboardPreview({ dark, toggleTheme, onTeam }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)

  const navItems = [
    { to: '/', label: 'Overview', icon: LayoutDashboard },
    { to: '/keys', label: 'Key Management', icon: KeyRound },
    { to: '/blockchain', label: 'Blockchain Explorer', icon: Blocks },
    { to: '/audit', label: 'Audit Logs', icon: Activity },
  ]

  return (
    <div className="dashboard-shell">
      <aside className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
        <Brand />
        <div className="sidebar-label">WORKSPACE</div>
        <nav className="sidebar-nav">
          {navItems.map(item => {
            const Icon = item.icon
            return <NavLink key={item.to} to={item.to} end={item.to === '/'} onClick={() => setMenuOpen(false)}><Icon size={19} />{item.label}</NavLink>
          })}
        </nav>
        <div className="sidebar-security">
          <div className="security-symbol"><ShieldCheck size={22} /></div>
          <strong>Security first</strong>
          <p>Keep sensitive key material protected and access controlled.</p>
        </div>
        <div className="sidebar-bottom"><span className="online-dot" /> Workspace UI preview</div>
      </aside>

      <div className="dashboard-main">
        <header className="dashboard-header">
          <button className="icon-button mobile-menu" onClick={() => setMenuOpen(value => !value)} aria-label="Open menu"><Menu /></button>
          <div><span className="muted header-kicker">HEALTHCARE KEY MANAGEMENT</span><h2>Security workspace</h2></div>
          <div className="header-actions">
            <button className="icon-button" onClick={toggleTheme} aria-label="Toggle theme">{dark ? <Sun size={19} /> : <Moon size={19} />}</button>
            <div className="profile-wrap">
              <button className="profile-button" onClick={() => setProfileOpen(value => !value)}>
                <span className="profile-avatar">N</span><span>Nicode2707</span><ChevronDown size={16} />
              </button>
              {profileOpen && <div className="profile-menu">
                <strong>Nicode2707</strong><span className="muted">Workspace account</span>
                <Link to="/login">Sign in</Link><Link to="/signup">Create account</Link>
              </div>}
            </div>
          </div>
        </header>

        <main className="dashboard-content">
          <div className="welcome-row">
            <div><span className="eyebrow">SECURE OPERATIONS</span><h1>Good to see you.</h1><p className="muted">Your healthcare key security workspace.</p></div>
            <span className="environment-badge"><span className="online-dot" /> Local environment</span>
          </div>

          <div className="notice-banner"><ShieldCheck size={22} /><div><strong>Security is built into every layer</strong><p>This is the Day 1 interface foundation. Live backend values will be connected and displayed in Day 2 and later.</p></div></div>

          <section className="stat-grid">
            <article className="stat-card"><span className="stat-icon teal"><KeyRound /></span><span className="muted">Managed key versions</span><strong>—</strong><small>Awaiting live API connection</small></article>
            <article className="stat-card"><span className="stat-icon green"><ShieldCheck /></span><span className="muted">Active keys</span><strong>—</strong><small>Awaiting live API connection</small></article>
            <article className="stat-card"><span className="stat-icon purple"><Blocks /></span><span className="muted">Blockchain status</span><strong>Not checked</strong><small>Will use the backend connection endpoint</small></article>
            <article className="stat-card"><span className="stat-icon orange"><Activity /></span><span className="muted">Audit events</span><strong>—</strong><small>Awaiting live API connection</small></article>
          </section>

          <section className="content-grid">
            <article className="panel"><div className="panel-heading"><div><h3>Key lifecycle</h3><p className="muted">Available lifecycle operations</p></div><KeyRound /></div><div className="empty-state"><div className="empty-icon"><KeyRound size={25} /></div><strong>No live key data connected yet</strong><p>Connect to the backend in the next implementation step.</p><Link className="button button-outline" to="/keys">View key management <ArrowRight size={16} /></Link></div></article>
            <article className="panel"><div className="panel-heading"><div><h3>Blockchain evidence</h3><p className="muted">Integrity and node connectivity</p></div><Blocks /></div><div className="blockchain-preview"><span className="blockchain-orb"><Blocks size={28} /></span><strong>Verification-ready architecture</strong><p>Lifecycle evidence is recorded through the backend and smart contract.</p><Link to="/blockchain" className="text-link">Explore blockchain <ArrowRight size={15} /></Link></div></article>
          </section>

          <div className="dashboard-footer"><Footer onTeam={onTeam} /></div>
        </main>
      </div>
    </div>
  )
}

function PlaceholderPage({ title, description }) {
  return <div className="placeholder-page"><div className="empty-icon"><KeyRound size={26} /></div><span className="eyebrow">WORKSPACE MODULE</span><h1>{title}</h1><p>{description}</p><span className="environment-badge">Integration planned for the next implementation steps</span><Link className="button button-outline" to="/">Back to overview</Link></div>
}

function App() {
  const [dark, setDark] = useState(false)
  const [teamOpen, setTeamOpen] = useState(false)

  const toggleTheme = () => setDark(value => !value)

  return (
    <div className={dark ? 'app theme-dark' : 'app theme-light'}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<AuthPage mode="login" dark={dark} toggleTheme={toggleTheme} />} />
          <Route path="/signup" element={<AuthPage mode="signup" dark={dark} toggleTheme={toggleTheme} />} />
          <Route path="/" element={<DashboardPreview dark={dark} toggleTheme={toggleTheme} onTeam={() => setTeamOpen(true)} />} />
          <Route path="/keys" element={<PlaceholderPage title="Key Management" description="Create, rotate, revoke and inspect key metadata using the existing Spring Boot API." />} />
          <Route path="/blockchain" element={<PlaceholderPage title="Blockchain Explorer" description="Inspect node connectivity and lifecycle integrity evidence through backend endpoints." />} />
          <Route path="/audit" element={<PlaceholderPage title="Audit Logs" description="Review authorized audit events from the backend." />} />
          <Route path="*" element={<PlaceholderPage title="Page not found" description="The requested workspace page does not exist." />} />
        </Routes>
      </BrowserRouter>
      {teamOpen && <TeamModal onClose={() => setTeamOpen(false)} />}
    </div>
  )
}

export default App
