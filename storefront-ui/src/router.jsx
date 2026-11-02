import { useSyncExternalStore } from 'react'

// Tiny router, no library needed. Both URL styles work:
//   /#/login  or  /login     -> login page
//   /#/signup or  /signup    -> sign-up page
//   anything else (#features, #top, /) -> the landing page,
//   so the landing page's in-page anchor links keep working.

const PAGES = ['/login', '/signup']

function subscribe(callback) {
  window.addEventListener('hashchange', callback)
  window.addEventListener('popstate', callback)
  return () => {
    window.removeEventListener('hashchange', callback)
    window.removeEventListener('popstate', callback)
  }
}

function getPath() {
  const { hash, pathname } = window.location

  if (hash.startsWith('#/')) return hash.slice(1)

  const path = pathname.replace(/\/+$/, '') || '/'
  return PAGES.includes(path) ? path : '/'
}

export function useRoute() {
  return useSyncExternalStore(subscribe, getPath)
}
