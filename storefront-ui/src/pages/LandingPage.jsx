import { useEffect } from 'react'
import Navbar from '../components/Navbar'
import Hero from '../components/Hero'
import HowItWorks from '../components/HowItWorks'
import NearbyMap from '../components/NearbyMap'
import Features from '../components/Features'
import CallToAction from '../components/CallToAction'
import Footer from '../components/Footer'
import '../styles/landing.css'

// The first page a visitor sees.
export default function LandingPage() {
  // Coming back from another page with a link like #features:
  // the section did not exist yet when the browser tried to scroll, so do it now.
  useEffect(() => {
    const target = window.location.hash && document.querySelector(window.location.hash)
    if (target) target.scrollIntoView()
    else window.scrollTo(0, 0)
  }, [])

  return (
    <>
      <Navbar />
      <main>
        <Hero />
        <HowItWorks />
        <NearbyMap />
        <Features />
        <CallToAction />
      </main>
      <Footer />
    </>
  )
}
