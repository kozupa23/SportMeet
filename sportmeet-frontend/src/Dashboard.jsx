import { useTranslation } from 'react-i18next';
import './Dashboard.css';

export default function Dashboard() {
  const { t } = useTranslation();

  // Definice dlaždic - pro přidání nové podstránky stačí rozšířit toto pole
  const tilesData = [
    {
      id: 'register',
      title: 'Registrace',
      description: 'Založte si nový uživatelský účet v aplikaci.',
      link: '/register',
      linkText: 'Přejít na formulář →'
    },
    {
      id: 'login',
      title: 'Přihlášení',
      description: 'Již máte účet? Přihlaste se do systému.',
      link: '/login',
      linkText: 'Přejít na přihlášení →'
    },
    // Zde můžete jednoduše přidat další dlaždici:
    // {
    //   id: 'events',
    //   title: 'Sportovní akce',
    //   description: 'Prohlédněte si seznam nadcházejících událostí.',
    //   link: '/events',
    //   linkText: 'Zobrazit akce →'
    // }
  ];

  return (
    <div className="dashboard-container">
      {/* Levá strana - Branding (stejný styl jako u formuláře) */}
      <div className="register-banner">
        <h1>{t('register.brandName', 'SportMeet')}</h1>
        <p>{t('register.brandSubtitle', 'Sportovní skupiny, akce a účast přehledně na jednom místě.')}</p>
      </div>

      {/* Pravá strana - Rozcestník s dlaždicemi */}
      <div className="dashboard-content-wrapper">
        <div className="tiles-grid">
          {tilesData.map((tile) => (
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