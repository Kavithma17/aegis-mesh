// Form validation rules shared by the login and sign-up pages.
// Each rule returns an error message, or '' when the value is fine.

export function required(value, label) {
  return value.trim() ? '' : `${label} is required`
}

export function email(value) {
  if (!value.trim()) return 'Email is required'
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()) ? '' : 'Enter a valid email address'
}

// Sri Lankan mobile numbers: 07XXXXXXXX or +947XXXXXXXX
export function phone(value) {
  if (!value.trim()) return 'Phone number is required'
  const digits = value.replace(/[\s-]/g, '')
  return /^(?:0|\+94)7\d{8}$/.test(digits) ? '' : 'Enter a mobile number like 07X XXX XXXX'
}

export function password(value) {
  if (!value) return 'Password is required'
  return value.length >= 8 ? '' : 'Use at least 8 characters'
}

export function matches(value, other) {
  if (!value) return 'Please confirm your password'
  return value === other ? '' : 'Passwords do not match'
}

// 0 (empty) to 4 (strong), with a label for the strength meter.
export function passwordStrength(value) {
  if (!value) return { score: 0, label: '' }

  let score = 0
  if (value.length >= 8) score++
  if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++
  if (/\d/.test(value)) score++
  if (/[^A-Za-z0-9]/.test(value) || value.length >= 12) score++

  const labels = ['Too short', 'Weak', 'Fair', 'Good', 'Strong']
  return { score, label: labels[score] }
}

// Runs every rule and returns { field: message } for the failing ones.
export function validate(rules) {
  const errors = {}
  for (const [field, message] of Object.entries(rules)) {
    if (message) errors[field] = message
  }
  return errors
}
