import React, { useState, useEffect } from 'react';

interface CountdownProps {
  deadline: number; // This should be the deadline in PST timezone, as a timestamp
}

export const Countdown: React.FC<CountdownProps> = ({ deadline }) => {
  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  function calculateTimeLeft() {
    // Create a new Date object with the deadline
    const deadlineDate = new Date(deadline);

    // Convert the deadline to PST
    const pstDeadline = new Date(deadlineDate.toLocaleString("en-US", { timeZone: "America/Los_Angeles" }));

    // Get the current time in PST
    const now = new Date(new Date().toLocaleString("en-US", { timeZone: "America/Los_Angeles" }));

    const difference = pstDeadline.getTime() - now.getTime();
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