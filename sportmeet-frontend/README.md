# Spuštění

Otevření terminálu ve složce sportmeet-frontend

Nainstalování závislostí 
npm install

Spuštění vývojového serveru
npm run dev

Otevření stránky
http://localhost:5173/

## Backend connection

Vite proxies `/api` to `http://localhost:8080`. Start the backend with
`SESSION_COOKIE_SECURE=false` for local HTTP development (production defaults to
secure cookies). Start the frontend using `npm install` and `npm run dev`.
Registration uses `/api/v1/users`; login uses the login name, not email.
Production hosting must forward `/api` to the backend on the same origin.
