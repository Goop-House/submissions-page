import React, { useState, useEffect } from 'react';
import axios from '../axiosConfig';

const Login: React.FC = () => {
  const [countdown, setCountdown] = useState('CALCULATING...');
  const [isDeadlinePassed, setIsDeadlinePassed] = useState(false);

  const handleLogin = () => {
    window.location.href = 'https://submit.goop.house/auth/discord';
  };

  useEffect(() => {
    const fetchDeadline = async () => {
      try {
        const response = await axios.get('/api/deadline');
        const deadlineDate = new Date(response.data.deadline);
        updateCountdown(deadlineDate);
        const intervalId = setInterval(() => updateCountdown(deadlineDate), 1000);
        return () => clearInterval(intervalId);
      } catch (error) {
        console.error('Error fetching deadline:', error);
      }
    };

    fetchDeadline();
  }, []);

  const updateCountdown = (deadlineDate: Date) => {
    const now = new Date().getTime();
    const timeLeft = deadlineDate.getTime() - now;

    if (timeLeft < 0) {
      setCountdown('SUBMISSION PERIOD HAS ENDED. TIME IS A CONSTRUCT.');
      setIsDeadlinePassed(true);
    } else {
      const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
      const hours = Math.floor((timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((timeLeft % (1000 * 60)) / 1000);

      setCountdown(`TIME LEFT: ${days}D ${hours}H ${minutes}M ${seconds}S`);
    }
  };

  return (
    <div>
      <h1>GOOP HOUSE TRACK SUBMISSION</h1>
      <div id="login-status">STATUS: NOT LOGGED IN</div>
      <button id="login-button" onClick={handleLogin}>LOGIN WITH DISCORD</button>
      <div id="countdown">{countdown}</div>
    </div>
  );
};

export default Login;
