import { useState } from 'react'
import Icon from './Icon'

const POPULAR = ['Panadol', 'Insulin', 'Amoxicillin', 'Salbutamol']

const TRUST = [
  { icon: 'activity', label: 'Live stock' },
  { icon: 'shield', label: 'Fair reservations' },
  { icon: 'check', label: 'Pharmacist confirmed' },
]

export default function Hero() {
  const [query, setQuery] = useState('')

  function search(event) {
    event.preventDefault()
    // The search page is not built yet; this will navigate there later.
  }

  return (
    <section className="hero" id="top">
      {/* Decorative background shapes */}
      <span className="blob blob-1" aria-hidden="true" />
      <span className="blob blob-2" aria-hidden="true" />
      <span className="dots" aria-hidden="true" />

      <div className="container hero-grid">
        <div className="hero-text">
          <span className="hero-badge">
            <span className="pulse-dot" /> Live stock from partner pharmacies
          </span>

          <h1>
            Find your medicine <span className="gradient-text">before</span> you leave home.
          </h1>

          <p className="hero-sub">
            Search live stock across pharmacies, reserve it in seconds and pick it up. No more going
            from pharmacy to pharmacy.
          </p>

          <form className="hero-search" onSubmit={search} role="search">
            <Icon name="search" size={20} />
            <input
              placeholder="Search a medicine, e.g. Panadol"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              aria-label="Medicine name"
            />
            <button type="submit" className="btn btn-primary">
              Search
            </button>
          </form>

          <div className="popular">
            <span>Popular:</span>
            {POPULAR.map((name) => (
              <button key={name} type="button" className="chip" onClick={() => setQuery(name)}>
                {name}
              </button>
            ))}
          </div>

          <ul className="trust">
            {TRUST.map((item) => (
              <li key={item.label}>
                <Icon name={item.icon} size={16} />
                {item.label}
              </li>
            ))}
          </ul>
        </div>

        <PhoneMockup />
      </div>
    </section>
  )
}

// A phone showing the app, so visitors see what they get.
// The data is only an illustration.
function PhoneMockup() {
  return (
    <div className="hero-visual" aria-hidden="true">
      <div className="phone">
        <span className="phone-notch" />

        <div className="phone-screen">
          <div className="app-bar">
            <span className="logo-mark small">+</span>
            <strong>Aegis-Mesh</strong>
          </div>

          <div className="app-search">
            <Icon name="search" size={14} /> Panadol 500mg
          </div>

          <ul className="app-results">
            <li>
              <span>
                <strong>City Pharmacy</strong>
                <small>Colombo 03 · 0.8 km</small>
              </span>
              <em className="ok">12 left</em>
            </li>
            <li>
              <span>
                <strong>Lanka Health</strong>
                <small>Colombo 05 · 2.1 km</small>
              </span>
              <em className="low">2 left</em>
            </li>
          </ul>

          <div className="app-ticket">
            <div className="ticket-row">
              <span className="ticket-check">
                <Icon name="check" size={14} />
              </span>
              <span>
                <small>Reserved for you</small>
                <strong>Panadol 500mg × 2</strong>
              </span>
            </div>
            <div className="ticket-code">
              <small>Pickup code</small>
              <strong>RES-7BF15444</strong>
            </div>
          </div>
        </div>
      </div>

      <div className="floating-chip chip-top">
        <Icon name="store" size={16} /> 12 in stock nearby
      </div>
      <div className="floating-chip chip-bottom">
        <Icon name="bolt" size={16} /> Reserved in 2 seconds
      </div>
    </div>
  )
}
