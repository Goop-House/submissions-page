import React, { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';

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
}

export const AdminPanel: React.FC = () => {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchSubmissions();
  }, []);

  const [newDeadline, setNewDeadline] = useState('');


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

  const downloadFile = async (path: string, fileName: string) => {
    try {
      const { data, error } = await supabase.storage
        .from(path.startsWith('audio') ? 'audio' : 'artwork')
        .download(path);

      if (error) {
        throw error;
      }

      const blob = new Blob([data], { type: 'application/octet-stream' });
      const url = window.URL.createObjectURL(blob);
      
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading file:', error);
      alert('Failed to download file. Please try again.');
    }
  };

  const shuffleArray = (array: string[]) => {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
  };

  const downloadAllSubmissions = async () => {
    try {
      const zip = new JSZip();

      for (const submission of submissions) {
        const { data: audioData, error: audioError } = await supabase.storage
          .from('audio')
          .download(submission.audio_path);

        if (audioError) {
          console.error(`Error downloading audio for ${submission.song_name}:`, audioError);
          continue;
        }

        const artists = [submission.artist_name1, submission.artist_name2, submission.artist_name3]
          .filter(name => name) // Remove empty artist names
          .join(', ');

        const shuffledArtists = shuffleArray(artists.split(', ')).join(', ');
        const fileName = `${shuffledArtists} - ${submission.song_name}.mp3`;

        zip.file(fileName, audioData);

        if (submission.art_path) {
          const { data: artData, error: artError } = await supabase.storage
            .from('artwork')
            .download(submission.art_path);

          if (artError) {
            console.error(`Error downloading artwork for ${submission.song_name}:`, artError);
          } else {
            zip.file(`${shuffledArtists} - ${submission.song_name}_artwork.jpg`, artData);
          }
        }
      }

      const content = await zip.generateAsync({ type: 'blob' });
      saveAs(content, 'all_submissions.zip');

    } catch (error) {
      console.error('Error downloading all submissions:', error);
      setError('Failed to download all submissions. Please try again.');
    }
  };

  

  if (loading) {
    return <div>LOADING GOOP DATA...</div>;
  }

  if (error) {
    return <div>ERROR: {error}</div>;
  }

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

  return (
    <div className="admin-panel">
      <h2>ADMIN PANEL</h2>
      <button type="submit" onClick={downloadAllSubmissions} disabled={loading}>
        DOWNLOAD ALL SUBMISSIONS
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
                <button type="submit" onClick={() => downloadFile(submission.audio_path, `${submission.artist_name1} - ${submission.song_name}.mp3`)}>
                  DOWNLOAD
                </button>
              </td>
              <td>
                {submission.art_path ? (
                  <button type="submit" onClick={() => downloadFile(submission.art_path!, `${submission.artist_name1} - ${submission.song_name}_artwork.jpg`)}>
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