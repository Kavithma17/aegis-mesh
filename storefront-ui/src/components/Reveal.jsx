import { useEffect, useRef } from 'react'

// Fades its children in the first time they scroll into view.
// Usage: <Reveal delay={100}>...</Reveal>
export default function Reveal({ as: Tag = 'div', delay = 0, className = '', children, ...rest }) {
  const ref = useRef(null)

  useEffect(() => {
    const element = ref.current
    if (!element) return

    // The delay is only for the entrance; drop it afterwards so hover
    // effects on the same element respond immediately.
    const clearDelay = () => {
      element.style.transitionDelay = '0ms'
    }
    element.addEventListener('transitionend', clearDelay, { once: true })

    if (!('IntersectionObserver' in window)) {
      element.classList.add('is-visible')
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          element.classList.add('is-visible')
          observer.disconnect()
        }
      },
      { threshold: 0.15 },
    )

    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  return (
    <Tag ref={ref} className={`reveal ${className}`} style={{ transitionDelay: `${delay}ms` }} {...rest}>
      {children}
    </Tag>
  )
}
