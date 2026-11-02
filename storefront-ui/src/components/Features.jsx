import Icon from './Icon'
import Reveal from './Reveal'

const FEATURES = [
  {
    icon: 'activity',
    title: 'Live availability',
    text: 'Stock counts come straight from each pharmacy, so what you see is what is on the shelf.',
  },
  {
    icon: 'bolt',
    title: 'Instant, fair reservations',
    text: 'When many people want the last few packs, each one is given to exactly one person.',
  },
  {
    icon: 'clock',
    title: 'Track every step',
    text: 'Watch your request move from sent to reserved in real time.',
  },
  {
    icon: 'sparkles',
    title: 'Smart alternatives',
    text: 'Out of stock? Get suggestions for nearby pharmacies or same-ingredient medicines.',
    soon: true,
  },
]

export default function Features() {
  return (
    <section className="section features" id="features">
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">Features</span>
          <h2>Built for medicine shortages</h2>
          <p>Everything you need when the medicine you rely on is hard to find.</p>
        </Reveal>

        <div className="features-grid">
          {FEATURES.map((feature, index) => (
            <Reveal as="article" key={feature.title} className="feature-card" delay={index * 100}>
              <span className="feature-icon">
                <Icon name={feature.icon} size={22} />
              </span>
              <h3>
                {feature.title}
                {feature.soon && <span className="soon">Coming soon</span>}
              </h3>
              <p>{feature.text}</p>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}
