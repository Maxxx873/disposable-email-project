import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import { AuthProvider } from "react-oidc-context";

const oidcConfig = {
  authority: "http://localhost:9080/realms/disposable_email_project",
  client_id: "admin-ui-app",
  redirect_uri: "http://localhost:5173",
};

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AuthProvider {...oidcConfig}>
      <App />
    </AuthProvider>
  </React.StrictMode>
)
