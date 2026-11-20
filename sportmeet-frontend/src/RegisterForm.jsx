import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import './RegisterForm.css';

export default function RegisterForm() {
  const { t } = useTranslation();

  const [formData, setFormData] = useState({
    login: '',
    display_name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Logování citlivých údajů (hesla) odstraněno dle review
    // Zde později přijde logika pro KAN-126 (odeslání na backend)
  };

  return (
    <div className="register-container" data-testid="register-container">
      {/* Levá strana - Branding */}
      <div className="register-banner">
        <h1>{t('register.brandName', 'SportMeet')}</h1>
        <p>{t('register.brandSubtitle', 'Sportovní skupiny, akce a účast přehledně na jednom místě.')}</p>
      </div>

      {/* Pravá strana - Formulář */}
      <div className="register-form-wrapper">
        <div className="register-card">
          <h2>{t('register.title', 'Založit účet')}</h2>
          <p className="subtitle">{t('register.subtitle', 'Povinná pole jsou označena hvězdičkou.')}</p>

          <form onSubmit={handleSubmit} data-testid="register-form">
            <div className="form-group">
              <label htmlFor="login">{t('register.loginLabel', 'Uživatelské jméno (login)')} *</label>
              <input 
                id="login"
                type="text" 
                name="login"
                placeholder={t('register.loginPlaceholder', 'VasLogin')} 
                value={formData.login}
                onChange={handleChange}
                data-testid="input-login"
                required 
              />
            </div>

            <div className="form-group">
              <label htmlFor="display_name">{t('register.displayNameLabel', 'Zobrazované jméno')} *</label>
              <input 
                id="display_name"
                type="text" 
                name="display_name"
                placeholder={t('register.displayNamePlaceholder', 'Jméno Příjmení')} 
                value={formData.display_name}
                onChange={handleChange}
                data-testid="input-display-name"
                required 
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">{t('register.emailLabel', 'E-mail')} *</label>
              <input 
                id="email"
                type="email" 
                name="email"
                placeholder={t('register.emailPlaceholder', 'email@example.cz')} 
                value={formData.email}
                onChange={handleChange}
                data-testid="input-email"
                required 
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="password">{t('register.passwordLabel', 'Heslo')} *</label>
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

              <div className="form-group">
                <label htmlFor="confirmPassword">{t('register.confirmPasswordLabel', 'Potvrzení hesla')} *</label>
                <input 
                  id="confirmPassword"
                  type="password" 
                  name="confirmPassword"
                  placeholder="••••••••" 
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  data-testid="input-confirm-password"
                  required 
                />
              </div>
            </div>

            <button type="submit" className="submit-btn" data-testid="submit-btn">
              {t('register.submitBtn', 'Registrovat se')}
            </button>
          </form>

          <div className="login-link">
            <span>{t('register.hasAccount', 'Máte účet? ')}</span>
            <a href="/login" data-testid="link-login">{t('register.loginLink', 'Přejít na přihlášení →')}</a>
          </div>
        </div>
      </div>
    </div>
  );
}