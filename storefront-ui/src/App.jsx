import { useRoute } from './router'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'

// Picks the page from the URL hash (see router.jsx).
export default function App() {
  const path = useRoute()

  if (path === '/login') return <LoginPage />
  if (path === '/signup') return <SignupPage />
  return <LandingPage />
}
