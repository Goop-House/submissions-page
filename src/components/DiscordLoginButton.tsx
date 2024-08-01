import React from 'react';
import { supabase } from '../supabaseClient';

export const DiscordLoginButton: React.FC = () => {
  const handleLogin = async () => {
    try {
      const { data, error } = await supabase.auth.signInWithOAuth({
        provider: 'discord',
        options: {
          redirectTo: window.location.origin, // Redirect back to your app after login
        },
      });

      if (error) {
        throw error;
      }

      // The user will be redirected to Discord for authentication.
      // After successful authentication, they will be redirected back to your app.
      // You don't need to handle the redirect here, as Supabase will do it automatically.

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