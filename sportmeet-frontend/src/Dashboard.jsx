import {useEffect, useState} from 'react';
import {currentUser, logout} from './api/auth';
import {useTranslation} from 'react-i18next';
import './Dashboard.css';

export default function Dashboard() {
  const { t } = useTranslation();

    const [user, setUser] = useState(null);
    const [error, setError] = useState('');
    useEffect(() => {
        let active = true;
        currentUser().then(user => {
            if (active) setUser(user);
        })
            .catch(error => {
                if (active) setError(error.message);
            });
        return () => {
            active = false;
        };
    }, []);

    async function handleLogout() {
        try {
            await logout();
            setUser(null);
            setError('');
        } catch (error) {
            setError(error.message);
        }
    }

  // Definice dlaždic - pro přidání nové podstránky stačí rozšířit toto pole
  const tilesData = [
    {
      id: 'register',
        title: t('dashboard.tile1'),
        description: t('dashboard.tile2'),
      link: '/register',
        linkText: t('dashboard.tile3')
    },
    {
      id: 'login',
        title: t('dashboard.tile4'),
        description: t('dashboard.tile5'),
      link: '/login',
        linkText: t('dashboard.tile6')
    },
  ];

  return (
    <div className="dashboard-container">
      {/* Levá strana - Branding (stejný styl jako u formuláře) */}
      <div className="register-banner">
          <h1>{t('register.brandName')}</h1>
          <p>{t('register.brandSubtitle')}</p>
      </div>

      {/* Pravá strana - Rozcestník s dlaždicemi */}
      <div className="dashboard-content-wrapper">
        <div className="tiles-grid">
            {error && <p role="alert">{error}</p>}
            {user && <div className="tile-card"><h3>{user.displayName || user.login}</h3><p>{user.email}</p>
                <button onClick={handleLogout}>{t('dashboard.logout')}</button>
            </div>}

            {!user && tilesData.map((tile) => (
            <a key={tile.id} href={tile.link} className="tile-card">
              <div>
                <h3>{tile.title}</h3>
                <p>{tile.description}</p>
              </div>
              <span className="tile-link-text">{tile.linkText}</span>
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}
