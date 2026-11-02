import Icon from './Icon'
import Reveal from './Reveal'
import mapImg from '../assets/illustrations/map.svg'

const LEGEND = [
  { tone: 'ok', label: 'In stock' },
  { tone: 'low', label: 'Running low' },
  { tone: 'out', label: 'Out of stock' },
]

export default function NearbyMap() {
  return (
    <section className="section nearby">
      <div className="container split">
        <Reveal className="split-text">
          <span className="eyebrow">Nearby</span>
          <h2>See who has it, before you go</h2>
          <p>
            Every pharmacy shows how many packs are left right now. Pick the closest one with stock
            and skip the guesswork.
          </p>

          <ul className="legend">
            {LEGEND.map((item) => (
              <li key={item.tone}>
                <span className={`legend-dot ${item.tone}`} />
                {item.label}
              </li>
            ))}
          </ul>

          <a href="#top" className="btn btn-outline">
            Find a pharmacy <Icon name="arrow" size={18} />
          </a>
        </Reveal>

        <Reveal className="split-image" delay={150}>
          <img src={mapImg} alt="Map showing nearby pharmacies and how much stock each one has" />
        </Reveal>
      </div>
    </section>
  )
}
