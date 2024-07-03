import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User } from 'firebase/auth';
import styled from 'styled-components';
import { auth } from '../firebaseConfig';

const Nav = styled.nav`
  ul {
    list-style-type: none;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  li {
    margin: 0 10px;
  }

  a {
    color: #9A00F5;
    text-decoration: none;
    &:hover {
      text-shadow: 0 0 10px #9A00F5;
    }
  }
`;

const LoginStatus = styled.div`
  text-align: center;
  margin-bottom: 20px;
`;

interface HeaderProps {
    user: User | null;
    isAdmin: boolean;
  }
  
  const Header: React.FC<HeaderProps> = ({ user, isAdmin }) => {
    const navigate = useNavigate();
  
    const handleLogout = async () => {
      try {
        await auth.signOut();
        navigate('/login');
      } catch (error) {
        console.error('Error during logout:', error);
      }
    };
  

  return (
    <header>
      <LoginStatus>
        STATUS: {user ? 'LOGGED IN' : 'NOT LOGGED IN'}
      </LoginStatus>
      <Nav>
        <ul>
          <li><Link to="/">HOME</Link></li>
          {isAdmin && <li><Link to="/admin">ADMIN VOID</Link></li>}
          {user ? (
            <>
              <li>WELCOME, {user.displayName?.toUpperCase()}</li>
              <li><button onClick={handleLogout}>ESCAPE THE VOID</button></li>
            </>
          ) : (
            <li><Link to="/login">ENTER THE VOID</Link></li>
          )}
        </ul>
      </Nav>
    </header>
  );
};

export default Header;