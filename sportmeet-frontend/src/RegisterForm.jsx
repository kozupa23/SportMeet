import {validateRegistration} from './api/validation';
import {registerUser} from './api/users';
import {Link, useNavigate} from 'react-router-dom';
import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import './RegisterForm.css';

export default function RegisterForm() {
  const { t } = useTranslation();
    const navigate = useNavigate();
    const [error, setError] = useState('');
    const [pending, setPending] = useState(false);
    const [touched, setTouched] = useState({});
    const [showPassword, setShowPassword] = useState(false);

  const [formData, setFormData] = useState({
    login: '',
    display_name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

    const fieldErrors = validateRegistration(formData);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

    const handleSubmit = async (e) => {
    e.preventDefault();
        if (pending) return;
        setError('');
        setTouched(Object.fromEntries(Object.keys(formData).map(key => [key, true])));
        if (Object.keys(fieldErrors).length) return;
        setPending(true);
        try {
            await registerUser({...formData, fullName: formData.display_name});
            navigate('/login', {state: {registered: true}});
        } catch (error) {
            setError(error.message);
        } finally {
            setPending(false);
        }
  };

  return (
      <div className="register-container registration-page" data-testid="register-container">
      {/* Levá strana - Branding s odkazem na hlavní stránku */}
      <div className="register-banner">
          <Link to="/" className="brand-home-link" style={{ textDecoration: 'none', color: 'inherit' }}>
              <h1>{t('register.brandName')}</h1>
              <p>{t('register.brandSubtitle')}</p>
          </Link>
      </div>

      {/* Pravá strana - Formulář */}
      <div className="register-form-wrapper">
        <div className="register-card">
            <h2>{t('register.title')}</h2>
            <p className="subtitle">{t('register.subtitle')}</p>

            <p role="alert">{error}</p>
            <form noValidate onSubmit={handleSubmit} data-testid="register-form">
            <div className="form-group">
                <label htmlFor="login">{t('register.loginLabel')} *</label>
                <input
                id="login"
                type="text"
                name="login"
                placeholder={t('register.loginPlaceholder')}
                value={formData.login}
                onBlur={() => setTouched(previous => ({...previous, login: true}))}
                aria-invalid={Boolean(touched.login && fieldErrors.login)}
                aria-describedby="login-error"
                onChange={handleChange}
                data-testid="input-login"
                required
              />
                {touched.login && fieldErrors.login &&
                    <small className="field-error" id="login-error" role="alert">{fieldErrors.login}</small>}
            </div>

            <div className="form-group">
                <label htmlFor="display_name">{t('register.displayNameLabel')} *</label>
                <input
                id="display_name"
                type="text"
                name="display_name"
                placeholder={t('register.displayNamePlaceholder')}
                value={formData.display_name}
                onBlur={() => setTouched(previous => ({...previous, display_name: true}))}
                aria-invalid={Boolean(touched.display_name && fieldErrors.display_name)}
                aria-describedby="display_name-error"
                onChange={handleChange}
                data-testid="input-display-name"
                required
              />
                {touched.display_name && fieldErrors.display_name &&
                    <small className="field-error" id="display_name-error"
                           role="alert">{fieldErrors.display_name}</small>}
            </div>

            <div className="form-group">
                <label htmlFor="email">{t('register.emailLabel')} *</label>
                <input
                id="email"
                type="email"
                name="email"
                placeholder={t('register.emailPlaceholder')}
                value={formData.email}
                onBlur={() => setTouched(previous => ({...previous, email: true}))}
                aria-invalid={Boolean(touched.email && fieldErrors.email)}
                aria-describedby="email-error"
                onChange={handleChange}
                data-testid="input-email"
                required
              />
                {touched.email && fieldErrors.email &&
                    <small className="field-error" id="email-error" role="alert">{fieldErrors.email}</small>}
            </div>

            <div className="form-row">
              <div className="form-group">
                  <label htmlFor="password">{t('register.passwordLabel')} *</label>
                  <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  name="transition" // opraveno níže na name="password"
                  name="password"
                  placeholder={t('common.passwordPlaceholder')}
                  value={formData.password}
                  onBlur={() => setTouched(previous => ({...previous, password: true}))}
                  aria-invalid={Boolean(touched.password && fieldErrors.password)}
                  aria-describedby="password-error"
                  onChange={handleChange}
                  data-testid="input-password"
                  required
                />
                  {touched.password && fieldErrors.password &&
                      <small className="field-error" id="password-error" role="alert">{fieldErrors.password}</small>}
              </div>

              <div className="form-group">
                  <label htmlFor="confirmPassword">{t('register.confirmPasswordLabel')} *</label>
                  <input
                  id="confirmPassword"
                  type={showPassword ? "text" : "password"}
                  name="confirmPassword"
                  placeholder={t('common.passwordPlaceholder')}
                  value={formData.confirmPassword}
                  onBlur={() => setTouched(previous => ({...previous, confirmPassword: true}))}
                  aria-invalid={Boolean(touched.confirmPassword && fieldErrors.confirmPassword)}
                  aria-describedby="confirmPassword-error"
                  onChange={handleChange}
                  data-testid="input-confirm-password"
                  required
                />
                  {touched.confirmPassword && fieldErrors.confirmPassword &&
                      <small className="field-error" id="confirmPassword-error"
                             role="alert">{fieldErrors.confirmPassword}</small>}
              </div>
            </div>

                <p className="password-help">{t('register.passwordHelp')}</p>
                <label><input type="checkbox" checked={showPassword}
                              onChange={e => setShowPassword(e.target.checked)}/>{t('register.showPasswords')}</label>
                <button disabled={pending} type="submit" className="submit-btn" data-testid="submit-btn">
                    {t('register.submitBtn')}
            </button>
          </form>

          <div className="login-link">
              <span>{t('register.hasUser')}</span>
              <Link to="/login" data-testid="link-login">{t('register.loginLink')}</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
