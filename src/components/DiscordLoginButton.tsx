import React from 'react';
import { supabase } from '../supabaseClient';

export const DiscordLoginButton: React.FC = () => {
  const handleLogin = async () => {
    try {
      const { data, error } = await supabase.auth.signInWithOAuth({
        provider: 'discord',
        options: {
          redirectTo: window.location.origin, 
        },
      });

      if (error) {
        throw error;
      }

    } catch (error) {
      console.error('Error logging in with Discord:', error);
      alert('Failed to log in with Discord. Please try again.');
    }
  };

  return (
    <button id="login-button" onClick={handleLogin}>
      LOGIN WITH DISCORD
    </button>
  );
};