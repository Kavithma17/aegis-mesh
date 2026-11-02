import Icon from './Icon'
import Reveal from './Reveal'
import pharmacyImg from '../assets/illustrations/pharmacy.svg'

export default function CallToAction() {
  return (
    <section className="section about" id="about">
      <div className="container">
        <div className="split story">
          <Reveal className="split-image">
            <img src={pharmacyImg} alt="Illustration of a neighbourhood pharmacy" />
          </Reveal>

          <Reveal className="split-text" delay={150}>
            <span className="eyebrow">Why we built this</span>
            <h2>One trip, not ten</h2>
            <p>
              During Sri Lanka&apos;s 2022 medicine shortage, people travelled from pharmacy to
              pharmacy looking for medicines they depended on. Aegis-Mesh shows live stock and lets
              you reserve it, so that trip only happens once.
            </p>
          </Reveal>
        </div>

        <Reveal className="cta">
          <span className="cta-ring ring-1" aria-hidden="true" />
          <span className="cta-ring ring-2" aria-hidden="true" />
          <div>
            <h2>Ready to find your medicine?</h2>
            <p>Search live stock now. It is free and takes seconds.</p>
          </div>
          <a href="#top" className="btn btn-white">
            Start searching <Icon name="arrow" size={18} />
          </a>
        </Reveal>
      </div>
    </section>
  )
}
