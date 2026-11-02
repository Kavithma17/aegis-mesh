import Reveal from './Reveal'
import searchImg from '../assets/illustrations/step-search.svg'
import reserveImg from '../assets/illustrations/step-reserve.svg'
import pickupImg from '../assets/illustrations/step-pickup.svg'

const STEPS = [
  {
    image: searchImg,
    title: 'Search',
    text: 'Type the medicine name and see which pharmacies have it right now.',
  },
  {
    image: reserveImg,
    title: 'Reserve',
    text: 'Pick a pharmacy and reserve. The stock is held for you, so nobody else takes it.',
  },
  {
    image: pickupImg,
    title: 'Pick up',
    text: 'Show your pickup code at the pharmacy and collect your medicine.',
  },
]

export default function HowItWorks() {
  return (
    <section className="section how" id="how-it-works">
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">How it works</span>
          <h2>Three steps, no wasted trips</h2>
          <p>From search to pickup in under a minute.</p>
        </Reveal>

        <ol className="steps-grid">
          {STEPS.map((step, index) => (
            <Reveal as="li" key={step.title} className="step-card" delay={index * 120}>
              <img src={step.image} alt="" className="step-image" />
              <div className="step-body">
                <span className="step-number">Step {index + 1}</span>
                <h3>{step.title}</h3>
                <p>{step.text}</p>
              </div>
            </Reveal>
          ))}
        </ol>
      </div>
    </section>
  )
}
