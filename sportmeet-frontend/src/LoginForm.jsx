import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import './LoginForm.css';

export default function LoginForm() {
  const { t } = useTranslation();

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Zde později přijde logika pro přihlášení (např. volání API)
  };

  return (
    <div className="register-container" data-testid="login-container">
      {/* Levá strana - Branding (shodný s RegisterForm) */}
      <div className="register-banner">
        <h1>{t('register.brandName', 'SportMeet')}</h1>
        <p>{t('login.brandSubtitle', 'Vytvářej sportovní skupiny, plánuj akce a potvrzuj účast.')}</p>
      </div>

      {/* Pravá strana - Přihlašovací formulář */}
      <div className="register-form-wrapper">
        <div className="register-card">
          <h2>{t('login.title', 'Přihlášení')}</h2>

          <form onSubmit={handleSubmit} data-testid="login-form">
            <div className="form-group">
              <label htmlFor="email">{t('login.emailLabel', 'E-mail')} *</label>
              <input 
                id="email"
                type="email" 
                name="email"
                placeholder={t('login.emailPlaceholder', 'email@example.cz')} 
                value={formData.email}
                onChange={handleChange}
                data-testid="input-email"
                required 
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">{t('login.passwordLabel', 'Heslo')} *</label>
              <input 
                id="password"
                type="password" 
                name="password"
                placeholder="••••••••" 
                value={formData.password}
                onChange={handleChange}
                data-testid="input-password"
                required 
              />
            </div>

            <button type="submit" className="submit-btn" data-testid="submit-btn">
              {t('login.submitBtn', 'Přihlásit se')}
            </button>
          </form>

          <div className="login-link">
            <span>{t('login.noAccount', 'Nemáte účet?')}</span>
            <Link to="/register" data-testid="link-register">
              {t('login.registerLink', 'Registrace →')}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}