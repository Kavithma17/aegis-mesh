import { useId } from 'react'
import Icon from './Icon'

// Label + input (or select) + error message, with an optional icon.
// Usage:
//   <FormField label="Email" icon="mail" error={errors.email} value={...} onChange={...} />
//   <FormField as="select" label="District" ...><option>...</option></FormField>
export default function FormField({ label, icon, error, hint, as = 'input', children, trailing, ...props }) {
  const id = useId()
  const Control = as

  return (
    <div className={`field ${error ? 'has-error' : ''}`}>
      <label htmlFor={id}>{label}</label>

      <div className="control">
        {icon && <Icon name={icon} size={18} className="control-icon" />}
        <Control
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={error || hint ? `${id}-msg` : undefined}
          {...props}
        >
          {children}
        </Control>
        {trailing}
      </div>

      {(error || hint) && (
        <p id={`${id}-msg`} className={error ? 'field-error' : 'field-hint'}>
          {error || hint}
        </p>
      )}
    </div>
  )
}
