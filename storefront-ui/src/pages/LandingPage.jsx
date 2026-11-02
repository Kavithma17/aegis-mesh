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
