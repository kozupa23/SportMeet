import { useState } from 'react';
import './RegisterForm.css'; // Případné styly

export default function RegisterForm() {
  const [formData, setFormData] = useState({
    fullName: '',
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
    // Zde později přijde logika pro KAN-126 (odeslání na backend)
    console.log("Odesílaná data:", formData);
  };

  return (
    <div className="register-container">
      {/* Levá strana - Branding */}
      <div className="register-banner">
        <h1>SportMeet</h1>
        <p>Sportovní skupiny, akce a účast přehledně na jednom místě.</p>
      </div>

      {/* Pravá strana - Formulář */}
      <div className="register-form-wrapper">
        <div className="register-card">
          <h2>Založit účet</h2>
          <p className="subtitle">Povinná pole jsou označena hvězdičkou.</p>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Jméno a příjmení *</label>
              <input 
                type="text" 
                name="fullName"
                placeholder="Jméno Příjmení" 
                value={formData.fullName}
                onChange={handleChange}
                required 
              />
            </div>

            <div className="form-group">
              <label>E-mail *</label>
              <input 
                type="email" 
                name="email"
                placeholder="email@example.cz" 
                value={formData.email}
                onChange={handleChange}
                required 
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Heslo *</label>
                <input 
                  type="password" 
                  name="password"
                  placeholder="••••••••" 
                  value={formData.password}
                  onChange={handleChange}
                  required 
                />
              </div>

              <div className="form-group">
                <label>Potvrzení hesla *</label>
                <input 
                  type="password" 
                  name="confirmPassword"
                  placeholder="••••••••" 
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required 
                />
              </div>
            </div>

            {/* Místo pro chybové hlášky (příprava pro KAN-125) */}
            {/* <div className="error-box">Chyba: e-mail je již použit / hesla se neshodují.</div> */}

            <button type="submit" className="submit-btn">
              Registrovat se
            </button>
          </form>

          <div className="login-link">
            <span>Máte účet? </span>
            <a href="/login">Přejít na přihlášení →</a>
          </div>
        </div>
      </div>
    </div>
  );
}