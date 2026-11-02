import { useState } from 'react'
import Icon from './Icon'

const LINKS = [
  { href: '#how-it-works', label: 'How it works' },
  { href: '#features', label: 'Features' },
  { href: '#about', label: 'About' },
]

export default function Navbar() {
  const [open, setOpen] = useState(false)

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <a href="#top" className="logo">
          <span className="logo-mark">+</span>
          Aegis-Mesh
        </a>

        <nav className={`nav-links ${open ? 'open' : ''}`}>
          {LINKS.map((link) => (
            <a key={link.href} href={link.href} onClick={() => setOpen(false)}>
              {link.label}
            </a>
          ))}
          <div className="nav-auth">
            <a href="#/login" className="btn btn-ghost" onClick={() => setOpen(false)}>
              Log in
            </a>
            <a href="#/signup" className="btn btn-primary nav-cta" onClick={() => setOpen(false)}>
              Sign up
            </a>
          </div>
        </nav>

        <button
          className="nav-toggle"
          onClick={() => setOpen((v) => !v)}
          aria-label={open ? 'Close menu' : 'Open menu'}
          aria-expanded={open}
        >
          <Icon name={open ? 'x' : 'menu'} />
        </button>
      </div>
    </header>
  )
}
