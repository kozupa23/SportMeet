//import RegisterForm from './RegisterForm';

//function App() {
//  return (
//    <div className="App">
//      <RegisterForm />
//    </div>
//  );
//}

//export default App;

import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './Dashboard';
import RegisterForm from './RegisterForm';
import LoginForm from './LoginForm';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          {/* Hlavní stránka s dlaždicemi (rozcestník) */}
          <Route path="/" element={<Dashboard />} />
          
          {/* Stránka s vaším registračním formulářem */}
          <Route path="/register" element={<RegisterForm />} />
          
          {/* Případně sem můžete v budoucnu přidat přihlášení: */}
          <Route path="/login" element={<LoginForm />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;