import { useState } from 'react'
import AuthLayout from '../components/AuthLayout'
import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import Icon from '../components/Icon'
import * as rules from '../utils/validation'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '', remember: true })
  const [errors, setErrors] = useState({})
  const [notice, setNotice] = useState('')

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
    // Clear a field's error as soon as the user edits it
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }))
  }

  function submit(event) {
    event.preventDefault()

    const found = rules.validate({
      email: rules.email(form.email),
      password: rules.required(form.password, 'Password'),
    })
    setErrors(found)
    if (Object.keys(found).length > 0) return

    // TODO: send to the login service once it exists.
    setNotice('Login is not connected yet. It will work once the login service is added.')
  }

  return (
    <AuthLayout>
      <header className="auth-head">
        <h1>Welcome back</h1>
        <p>Log in to see and manage your reservations.</p>
      </header>

      <form className="auth-form" onSubmit={submit} noValidate>
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

        <PasswordField
          label="Password"
          autoComplete="current-password"
          placeholder="Your password"
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
          error={errors.password}
        />

        <div className="form-row">
          <label className="checkbox">
            <input
              type="checkbox"
              checked={form.remember}
              onChange={(e) => update('remember', e.target.checked)}
            />
            Remember me
          </label>
          <a href="#/login" className="text-link" onClick={(e) => e.preventDefault()}>
            Forgot password?
          </a>
        </div>

        {notice && (
          <p className="form-notice" role="status">
            <Icon name="info" size={18} /> {notice}
          </p>
        )}

        <button type="submit" className="btn btn-primary btn-block">
          Log in
        </button>
      </form>

      <p className="auth-switch">
        Don&apos;t have an account? <a href="#/signup">Sign up</a>
      </p>
    </AuthLayout>
  )
}
