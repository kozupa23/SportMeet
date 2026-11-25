import i18n from '../i18n.js';

export function validateRegistration(form) {
  const errors = {};
  const login = form.login.trim();
  if (login.length < 2 || login.length > 80) errors.login = i18n.t('validation.message1');
  const name = form.display_name.trim();
  if (!name || name.length > 255) errors.display_name = i18n.t('validation.message2');
  const email = form.email.trim();
  if (email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = i18n.t('validation.message3');
  if (!form.password.trim() || form.password.length < 8 || !/\p{Lu}/u.test(form.password) || !/[0-9]/.test(form.password) || new TextEncoder().encode(form.password).length > 72) {
    errors.password = i18n.t('validation.message4');
  }
  if (!form.confirmPassword) errors.confirmPassword = 'Potvrďte heslo.';
  else if (form.password !== form.confirmPassword) errors.confirmPassword = i18n.t('validation.message5');
  return errors;
}
