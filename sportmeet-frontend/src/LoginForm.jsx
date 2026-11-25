import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {login} from './api/auth';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import './LoginForm.css';

export default function LoginForm() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [pending, setPending] = useState(false);

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (pending) return;
    setError('');
    setPending(true);
    try {
      await login(formData.email.trim(), formData.password);
      navigate('/');
    } catch (error) {
      setError(error.message);
    } finally {
      setPending(false);
    }
  };

  return (
    <div className="register-container" data-testid="login-container">
      {/* Levá strana - Branding (shodný s RegisterForm) */}
      <div className="register-banner">
        <h1>{t('register.brandName')}</h1>
        <p>{t('login.brandSubtitle')}</p>
      </div>

      {/* Pravá strana - Přihlašovací formulář */}
      <div className="register-form-wrapper">
        <div className="register-card">
          <h2>{t('login.title')}</h2>

          {location.state?.registered && <p role="status">{t('login.registered')}</p>}
          <p role="alert">{error}</p>
          <form onSubmit={handleSubmit} data-testid="login-form">
            <div className="form-group">
              <label htmlFor="email">{t('login.emailLabel')} *</label>
              <input
                id="email"
                type="text"
                name="email"
                placeholder={t('login.emailPlaceholder')}
                value={formData.email}
                onChange={handleChange}
                data-testid="input-email"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">{t('login.passwordLabel')} *</label>
              <input
                id="password"
                type="password"
                name="password"
                placeholder={t('common.passwordPlaceholder')}
                value={formData.password}
                onChange={handleChange}
                data-testid="input-password"
                required
              />
            </div>

            <button disabled={pending} type="submit" className="submit-btn" data-testid="submit-btn">
              {t('login.submitBtn')}
            </button>
          </form>

          <div className="login-link">
            <span>{t('login.noUser')}</span>
            <Link to="/register" data-testid="link-register">
              {t('login.registerLink')}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
