import { useState } from 'react'
import AuthLayout from '../components/AuthLayout'
import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import Icon from '../components/Icon'
import * as rules from '../utils/validation'

const DISTRICTS = [
  'Ampara', 'Anuradhapura', 'Badulla', 'Batticaloa', 'Colombo', 'Galle', 'Gampaha',
  'Hambantota', 'Jaffna', 'Kalutara', 'Kandy', 'Kegalle', 'Kilinochchi', 'Kurunegala',
  'Mannar', 'Matale', 'Matara', 'Monaragala', 'Mullaitivu', 'Nuwara Eliya', 'Polonnaruwa',
  'Puttalam', 'Ratnapura', 'Trincomalee', 'Vavuniya',
]

const EMPTY = {
  fullName: '',
  pharmacyName: '',
  licenseNumber: '',
  district: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  terms: false,
}

export default function SignupPage() {
  const [role, setRole] = useState('patient')
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [notice, setNotice] = useState('')

  const isPharmacy = role === 'pharmacy'

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }))
  }

  function switchRole(next) {
    setRole(next)
    setErrors({})
    setNotice('')
  }

  function submit(event) {
    event.preventDefault()

    const found = rules.validate({
      fullName: rules.required(form.fullName, isPharmacy ? 'Contact name' : 'Full name'),
      pharmacyName: isPharmacy ? rules.required(form.pharmacyName, 'Pharmacy name') : '',
      licenseNumber: isPharmacy ? rules.required(form.licenseNumber, 'License number') : '',
      district: isPharmacy ? rules.required(form.district, 'District') : '',
      email: rules.email(form.email),
      phone: rules.phone(form.phone),
      password: rules.password(form.password),
      confirmPassword: rules.matches(form.confirmPassword, form.password),
      terms: form.terms ? '' : 'Please accept the terms to continue',
    })
    setErrors(found)
    if (Object.keys(found).length > 0) return

    // TODO: send to the login service once it exists.
    setNotice('Sign up is not connected yet. It will work once the login service is added.')
  }

  return (
    <AuthLayout>
      <header className="auth-head">
        <h1>Create your account</h1>
        <p>Free for patients. Pharmacies are verified before going live.</p>
      </header>

      <div className="role-tabs" role="tablist" aria-label="Account type">
        <button
          type="button"
          role="tab"
          aria-selected={!isPharmacy}
          className={!isPharmacy ? 'active' : ''}
          onClick={() => switchRole('patient')}
        >
          <Icon name="user" size={18} /> I&apos;m a patient
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={isPharmacy}
          className={isPharmacy ? 'active' : ''}
          onClick={() => switchRole('pharmacy')}
        >
          <Icon name="store" size={18} /> I&apos;m a pharmacy
        </button>
      </div>

      <form className="auth-form" onSubmit={submit} noValidate>
        {isPharmacy && (
          <>
            <FormField
              label="Pharmacy name"
              icon="store"
              placeholder="City Pharmacy"
              value={form.pharmacyName}
              onChange={(e) => update('pharmacyName', e.target.value)}
              error={errors.pharmacyName}
            />

            <div className="form-grid">
              <FormField
                label="License number"
                icon="badge"
                placeholder="e.g. PH/2024/0123"
                value={form.licenseNumber}
                onChange={(e) => update('licenseNumber', e.target.value)}
                error={errors.licenseNumber}
              />
              <FormField
                as="select"
                label="District"
                icon="mapPin"
                value={form.district}
                onChange={(e) => update('district', e.target.value)}
                error={errors.district}
              >
                <option value="">Select district</option>
                {DISTRICTS.map((d) => (
                  <option key={d}>{d}</option>
                ))}
              </FormField>
            </div>
          </>
        )}

        <FormField
          label={isPharmacy ? 'Contact person' : 'Full name'}
          icon="user"
          autoComplete="name"
          placeholder={isPharmacy ? 'Name of the pharmacist in charge' : 'Your full name'}
          value={form.fullName}
          onChange={(e) => update('fullName', e.target.value)}
          error={errors.fullName}
        />

        <div className="form-grid">
          <FormField
            label="Email"
            icon="mail"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            error={errors.email}
          />
          <FormField
            label="Mobile number"
            icon="phone"
            type="tel"
            autoComplete="tel"
            placeholder="07X XXX XXXX"
            value={form.phone}
            onChange={(e) => update('phone', e.target.value)}
            error={errors.phone}
          />
        </div>

        <PasswordField
          label="Password"
          autoComplete="new-password"
          placeholder="At least 8 characters"
          showStrength
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
          error={errors.password}
        />

        <PasswordField
          label="Confirm password"
          autoComplete="new-password"
          placeholder="Type it again"
          value={form.confirmPassword}
          onChange={(e) => update('confirmPassword', e.target.value)}
          error={errors.confirmPassword}
        />

        <div className={`terms ${errors.terms ? 'has-error' : ''}`}>
          <label className="checkbox">
            <input type="checkbox" checked={form.terms} onChange={(e) => update('terms', e.target.checked)} />
            I agree to the Terms of Service and Privacy Policy
          </label>
          {errors.terms && <p className="field-error">{errors.terms}</p>}
        </div>

        {isPharmacy && (
          <p className="form-info">
            <Icon name="shield" size={18} />
            An admin checks your license before your pharmacy appears in search.
          </p>
        )}

        {notice && (
          <p className="form-notice" role="status">
            <Icon name="info" size={18} /> {notice}
          </p>
        )}

        <button type="submit" className="btn btn-primary btn-block">
          {isPharmacy ? 'Register pharmacy' : 'Create account'}
        </button>
      </form>

      <p className="auth-switch">
        Already have an account? <a href="#/login">Log in</a>
      </p>
    </AuthLayout>
  )
}
