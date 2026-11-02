import { useState } from 'react'
import FormField from './FormField'
import Icon from './Icon'
import { passwordStrength } from '../utils/validation'

// Password input with a show/hide button and an optional strength meter.
export default function PasswordField({ showStrength = false, value, ...props }) {
  const [visible, setVisible] = useState(false)
  const strength = passwordStrength(value)

  return (
    <div className="password-field">
      <FormField
        {...props}
        value={value}
        icon="lock"
        type={visible ? 'text' : 'password'}
        trailing={
          <button
            type="button"
            className="toggle-visibility"
            onClick={() => setVisible((v) => !v)}
            aria-label={visible ? 'Hide password' : 'Show password'}
          >
            <Icon name={visible ? 'eyeOff' : 'eye'} size={18} />
          </button>
        }
      />

      {showStrength && value && (
        <div className={`strength strength-${strength.score}`} aria-live="polite">
          <div className="strength-bars">
            {[1, 2, 3, 4].map((n) => (
              <span key={n} className={n <= strength.score ? 'on' : ''} />
            ))}
          </div>
          <small>{strength.label}</small>
        </div>
      )}
    </div>
  )
}
