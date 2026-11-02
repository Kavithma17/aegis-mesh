import { useEffect } from 'react'
import Icon from './Icon'
import pharmacyImg from '../assets/illustrations/pharmacy.svg'
import '../styles/auth.css'

const POINTS = ['See live stock at every pharmacy', 'Reserve in seconds, pick up later', 'Track your request step by step']

// Split screen used by the login and sign-up pages:
// blue brand panel on the left, the form on the right.
export default function AuthLayout({ children }) {
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  return (
    <div className="auth">
      <aside className="auth-panel">
        <span className="auth-ring ring-a" aria-hidden="true" />
        <span className="auth-ring ring-b" aria-hidden="true" />

        <a href="/#top" className="logo logo-light">
          <span className="logo-mark">+</span>
          Aegis-Mesh
        </a>

        <div className="auth-panel-body">
          <img src={pharmacyImg} alt="" className="auth-image" />
          <h2>Your medicine, reserved before you arrive.</h2>
          <ul className="auth-points">
            {POINTS.map((point) => (
              <li key={point}>
                <Icon name="check" size={16} />
                {point}
              </li>
            ))}
          </ul>
        </div>

        <p className="auth-panel-foot">
          <Icon name="shield" size={16} /> Always confirm medicines with your pharmacist.
        </p>
      </aside>

      <main className="auth-main">
        <a href="/#top" className="back-link">
          <Icon name="back" size={16} /> Back to home
        </a>
        <div className="auth-card">{children}</div>
      </main>
    </div>
  )
}
