import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import SubmissionForm from './components/SubmissionForm';
import Login from './components/Login';
import { GlobalStyle } from './GlobalStyle';
import { AsciiArt } from './components/AsciiArt';
import Footer from './components/Footer';
import AdminPage from './components/AdminPage';
import CheckPermissions from './components/CheckPermissions';


const App: React.FC = () => {
  return (
    <Router>
      <GlobalStyle />
      <div className="App">
        <AsciiArt />
        <Routes>
          <Route path="/" element={<SubmissionForm />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/login" element={<Login />} />
        </Routes>
        <div className="goop">&gt; THE GOOP YEARNS TO CONSUME YOUR CREATION &lt;</div>
        <Footer />
      </div>
      {/* <CheckPermissions />  */}
    </Router>
  );
};

export default App;