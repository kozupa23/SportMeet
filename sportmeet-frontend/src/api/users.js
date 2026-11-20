import i18n from '../i18n.js';

export async function registerUser({ login, fullName, email, password }) {
  let response;
  try {
    response = await fetch('/api/v1/users', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        login: login.trim(),
        displayName: fullName.trim(),
        email: email.trim(),
        password,
      }),
    });
  } catch {
    throw new Error(i18n.t('users.message1'));
  }

  if (response.status === 201) return;
  if (response.status === 409) {
    throw new Error(i18n.t('users.message2'));
  }
  if (response.status === 400) {
    throw new Error(i18n.t('users.message3'));
  }
  throw new Error(i18n.t('users.message4'));
}
