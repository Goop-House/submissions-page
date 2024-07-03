import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../axiosConfig';

interface User {
  id: string;
  username: string;
  avatar: string;
}

interface Submission {
  id: string;
  artistName: string;
  songTitle: string;
  audioUrl: string;
  artUrl?: string;
}

const SubmissionForm: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [artistName, setArtistName] = useState('');
  const [songTitle, setSongTitle] = useState('');
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [artFile, setArtFile] = useState<File | null>(null);
  const [isDeadlinePassed, setIsDeadlinePassed] = useState(false);
  const [countdown, setCountdown] = useState('');
  const [submission, setSubmission] = useState<Submission | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchUser();
    fetchDeadline();
    fetchSubmission();
  }, []);

  const fetchUser = async () => {
    try {
      const response = await axios.get('/api/user');
      setUser(response.data);
    } catch (error) {
      navigate('/login');
    }
  };

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

  const fetchSubmission = async () => {
    try {
      const response = await axios.get('/api/submission');
      if (response.data) {
        setSubmission(response.data);
        setArtistName(response.data.artistName);
        setSongTitle(response.data.songTitle);
      }
    } catch (error) {
      console.error('Error fetching submission:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || isDeadlinePassed) return;

    const formData = new FormData();
    formData.append('artistName', artistName);
    formData.append('songTitle', songTitle);
    if (audioFile) formData.append('audio', audioFile);
    if (artFile) formData.append('art', artFile);

    try {
      const response = await axios.post('/api/submit', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setSubmission(response.data);
      alert('GOOP RECEIVED. PROCESSING IN ALTERNATE DIMENSION.');
    } catch (error) {
      console.error('Error submitting:', error);
      alert('ERROR SUBMITTING. THE VOID REJECTS YOUR OFFERING.');
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post('/api/logout');
      navigate('/login');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  if (!user) {
    return <div>LOADING...</div>;
  }

  if (isDeadlinePassed) {
    return (
      <div className="centered-message">
        <p>SUBMISSION WINDOW CLOSED. TIME IS A CONSTRUCT.</p>
        <button onClick={handleLogout}>LOGOUT</button>
      </div>
    );
  }
  return (
    <>
      <h1>GOOP HOUSE TRACK SUBMISSION</h1>
      <div id="login-status">STATUS: LOGGED IN</div>
      <button id="login-button" onClick={handleLogout}>LOGOUT</button>
      <div id="countdown">{countdown}</div>
      <form onSubmit={handleSubmit}>
        <label htmlFor="audio">AUDIO FILE (REQUIRED):</label>
        <input
          type="file"
          id="audio"
          onChange={(e) => setAudioFile(e.target.files?.[0] || null)}
          accept="audio/*"
          required={!submission}
        />
        
        <label htmlFor="art">ARTWORK (OPTIONAL):</label>
        <input
          type="file"
          id="art"
          onChange={(e) => setArtFile(e.target.files?.[0] || null)}
          accept="image/*"
        />
        
        <label htmlFor="song_name">SONG NAME:</label>
        <input
          type="text"
          id="song_name"
          value={songTitle}
          onChange={(e) => setSongTitle(e.target.value)}
          required
        />
        
        <label htmlFor="artist_name">ARTIST NAME:</label>
        <input
          type="text"
          id="artist_name"
          value={artistName}
          onChange={(e) => setArtistName(e.target.value)}
          required
        />
        
        <input type="submit" value={submission ? "UPDATE SUBMISSION" : "SUBMIT TO THE VOID"} />
      </form>
    </>
  );
};

export default SubmissionForm;