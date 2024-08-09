import React, { useState, useEffect } from 'react';

interface CountdownProps {
  deadline: number; // The deadline should be a Unix timestamp in milliseconds (UTC time)
}

export const Countdown: React.FC<CountdownProps> = ({ deadline }) => {
  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  function calculateTimeLeft() {
    const currentUtcTime = new Date().getTime();
    const difference = deadline - currentUtcTime;

    if (difference > 0) {
      return {
        days: Math.floor(difference / (1000 * 60 * 60 * 24)),
        hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
        minutes: Math.floor((difference / 1000 / 60) % 60),
        seconds: Math.floor((difference / 1000) % 60)
      };
    }
    return null;
  }

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  if (!timeLeft) {
    return <div id="countdown">SUBMISSION PERIOD HAS ENDED. TIME IS A CONSTRUCT.</div>;
  }

  return (
    <div id="countdown">
      TIME LEFT: {timeLeft.days}D {timeLeft.hours}H {timeLeft.minutes}M {timeLeft.seconds}S
    </div>
  );
};
