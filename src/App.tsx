import React, { useState, useEffect } from 'react';
import { supabase } from './supabaseClient';
import { Session } from '@supabase/supabase-js';
import { DiscordLoginButton } from './components/DiscordLoginButton';
import { SubmissionForm } from './components/SubmissionForm';
import { Countdown } from './components/Countdown';
import { AdminPanel } from './components/AdminPanel';
import { AsciiArt } from './components/AsciiArt';
import './App.css';

const App: React.FC = () => {
  const [session, setSession] = useState<Session | null>(null);
  const [user, setUser] = useState<any>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [submissionDeadline, setSubmissionDeadline] = useState<number | null>(null);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
      setUser(session?.user ?? null);
    });

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
      setUser(session?.user ?? null);
    });

    fetchDeadline();

    return () => subscription.unsubscribe();
  }, []);

  useEffect(() => {
    if (user) {
      checkAdminStatus(user.id);
    } else {
      setIsAdmin(false);
    }
  }, [user]);

  const fetchDeadline = async () => {
    const { data, error } = await supabase
      .from('settings')
      .select('value')
      .eq('key', 'submission_deadline')
      .single();

    if (error) {
      console.error('Error fetching deadline:', error);
    } else if (data) {
      // Interpret the deadline as a UTC timestamp
      const utcDeadline = Date.parse(data.value + "Z"); // Ensure the deadline is treated as UTC by appending "Z"
      setSubmissionDeadline(utcDeadline);
    }
  };

  const checkAdminStatus = async (userId: string) => {
    const { data, error } = await supabase
      .from('admins')
      .select('*')
      .eq('user_id', userId)
      .single();

    if (data && !error) {
      setIsAdmin(true);
    } else {
      setIsAdmin(false);
    }
  };

  const handleLogout = async () => {
    const { error } = await supabase.auth.signOut();
    if (error) {
      console.error('Error signing out:', error);
    }
  };

  return (
    <div className="app">
      <header>
        <div className="ascii-art">
          {<AsciiArt />}
        </div>
        <h1>GOOP HOUSE TRACK SUBMISSION</h1>
      </header>

      <div id="login-status">STATUS: {session ? 'LOGGED IN' : 'NOT LOGGED IN'}</div>
      {!session ? (
        <DiscordLoginButton />
      ) : (
        <button onClick={handleLogout} className="logout-button">LOGOUT</button>
      )}

      {submissionDeadline && <Countdown deadline={submissionDeadline} />}

      {session && submissionDeadline && (
        <SubmissionForm user={user} deadline={submissionDeadline} />
      )}

      {isAdmin && <AdminPanel />}

      <div className="goop">&gt; THE GOOP YEARNS TO CONSUME YOUR CREATION &lt;</div>

      <footer>
        <p>NAVIGATE THE GOOP:</p>
        <ul>
          <li><a href="https://www.twitch.tv/goop_house">TWITCH</a></li>
          <li><a href="https://discord.gg/qD2wbqqDGX">DISCORD</a></li>
          <li><a href="https://soundcloud.com/goophouse">MUSIC</a></li>
          <li><a href="https://my-store-bd8e00.creator-spring.com/">ARTIFACTS</a></li>
        </ul>
      </footer>
    </div>
  );
};

export default App;
