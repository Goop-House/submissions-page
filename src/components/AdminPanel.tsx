import React, { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';

interface Submission {
  id: string;
  user_id: string;
  song_name: string;
  artist_name1: string;
  artist_name2: string;
  artist_name3: string;
  audio_path: string;
  art_path: string | null;
  created_at: string;
  audioUrl: string;
  artworkUrl: string | null;
}

export const AdminPanel: React.FC = () => {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newDeadline, setNewDeadline] = useState('');

  useEffect(() => {
    fetchSubmissions();
  }, []);

  const fetchSubmissions = async () => {
    setLoading(true);
    setError(null);
    const { data, error } = await supabase
      .from('submissions')
      .select('*')
      .order('created_at', { ascending: false });

    if (error) {
      console.error('Error fetching submissions:', error);
      setError('Failed to fetch submissions. Please try again.');
    } else {
      setSubmissions(data as Submission[]);
    }
    setLoading(false);
  };

  const downloadSubmissionsJson = async () => {
    try {
      const { data: { session } } = await supabase.auth.getSession();
      const response = await fetch('https://qpovypmwxnwjaucsfujx.supabase.co/functions/v1/download-all-submissions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session?.access_token}`,
        },
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = 'submissions.json';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);

    } catch (error) {
      console.error('Error downloading submissions JSON:', error);
      setError('Failed to download submissions JSON. Please try again.');
    }
  };

  const downloadScript = async () => {
    try {
      const response = await fetch('https://github.com/Goop-House/submissions-page/blob/production/download_submissions.py');
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = 'download_submissions.py';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading script:', error);
      setError('Failed to download script. Please try again.');
    }
  };

  const updateDeadline = async () => {
    if (!newDeadline) {
      alert('Please enter a new deadline');
      return;
    }

    const { error } = await supabase
      .from('settings')
      .update({ value: newDeadline })
      .eq('key', 'submission_deadline');

    if (error) {
      console.error('Error updating deadline:', error);
      alert('Failed to update deadline. Please try again.');
    } else {
      alert('Deadline updated successfully');
      setNewDeadline('');
    }
  };

  if (loading) {
    return <div>LOADING GOOP DATA...</div>;
  }

  if (error) {
    return <div>ERROR: {error}</div>;
  }

  return (
    <div className="admin-panel">
      <h2>ADMIN PANEL</h2>
      <button type="button" onClick={downloadSubmissionsJson} disabled={loading}>
        DOWNLOAD SUBMISSIONS JSON
      </button>
      <button type="button" onClick={downloadScript}>
        DOWNLOAD DOWNLOADER SCRIPT
      </button>

      <div>
        <h3>Update Submission Deadline</h3>
        <input
          type="datetime-local"
          value={newDeadline}
          onChange={(e) => setNewDeadline(e.target.value)}
        />
        <button onClick={updateDeadline}>Update Deadline</button>
      </div>
      
      <table>
        <thead>
          <tr>
            <th>ARTISTS</th>
            <th>SONG NAME</th>
            <th>SUBMISSION DATE</th>
            <th>AUDIO</th>
            <th>ARTWORK</th>
          </tr>
        </thead>
        <tbody>
          {submissions.map((submission) => (
            <tr key={submission.id}>
              <td>{[submission.artist_name1, submission.artist_name2, submission.artist_name3].filter(Boolean).join(', ')}</td>
              <td>{submission.song_name}</td>
              <td>{new Date(submission.created_at).toLocaleString()}</td>
              <td>
                <button type="submit" onClick={() => window.open(`${supabase.storage.from('audio').getPublicUrl(submission.audio_path).data.publicUrl}`)}>
                  DOWNLOAD
                </button>
              </td>
              <td>
                {submission.art_path ? (
                  <button type="submit" onClick={() => window.open(`${supabase.storage.from('artwork').getPublicUrl(submission.art_path!).data.publicUrl}`)}>
                    DOWNLOAD
                  </button>
                ) : (
                  'N/A'
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
