import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import cs from './locales/cs.json' with { type: 'json' };

i18n.use(initReactI18next).init({
  resources: { cs: { translation: cs } },
  lng: 'cs',
  fallbackLng: 'cs',
  keySeparator: false,
  interpolation: { escapeValue: false },
});

export default i18n;
