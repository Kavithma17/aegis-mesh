import Icon from './Icon'

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-brand">
          <a href="#top" className="logo">
            <span className="logo-mark">+</span>
            Aegis-Mesh
          </a>
          <p>Find scarce medicines and reserve them for pickup.</p>
        </div>

        <p className="footer-note">
          <Icon name="shield" size={16} />
          Always confirm medicines and doses with your pharmacist.
        </p>
      </div>

      <div className="container footer-bottom">
        <span>© {new Date().getFullYear()} Aegis-Mesh</span>
        <span>
          Made with <Icon name="heart" size={14} /> in Sri Lanka
        </span>
      </div>
    </footer>
  )
}
