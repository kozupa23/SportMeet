import i18n from '../i18n.js';

async function request(path, options) {
  try { return await fetch(`/api/v1${path}`, { credentials: 'include', ...options }); }
  catch { throw new Error(i18n.t('auth.message1')); }
}

export async function login(login, password) {
  const csrf = await request('/auth/csrf');
  if (!csrf.ok) throw new Error(i18n.t('auth.message2'));
  const { headerName, token } = await csrf.json();
  const response = await request('/auth/login', {
    method: 'POST',
    headers: { [headerName]: token },
    body: new URLSearchParams({ login, password }),
  });
  if (!response.ok) throw new Error(response.status === 401
    ? i18n.t('auth.message3') : i18n.t('auth.message4'));
}

export async function currentUser() {
  const response = await request('/users/me');
  if (response.status === 401) return null;
  if (!response.ok) throw new Error(i18n.t('auth.message5'));
  return response.json();
}

export async function logout() {
  const csrf = await request('/auth/csrf');
  if (!csrf.ok) throw new Error(i18n.t('auth.message6'));
  const { headerName, token } = await csrf.json();
  const response = await request('/auth/logout', { method: 'POST', headers: { [headerName]: token } });
  if (!response.ok) throw new Error(i18n.t('auth.message7'));
}
