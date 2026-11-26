import i18n from './i18n.js'
import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'

document.title = i18n.t('register.brandName')
document.documentElement.lang = i18n.language

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
