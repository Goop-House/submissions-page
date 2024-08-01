import React, { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';

interface Submission {
  id: string;
  user_id: string;
  song_name: string;
  artist_name: string;
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

      // Create a blob from the file data
      const blob = new Blob([data], { type: 'application/octet-stream' });
      const url = window.URL.createObjectURL(blob);
      
      // Create a temporary anchor element and trigger the download
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

  // const downloadAllSubmissions = async () => {
  //   setLoading(true);
  //   setError(null);
  //   try {
  //     const { data, error } = await supabase.functions.invoke('download-all-submissions', {
  //       method: 'POST',
  //     });
  
  //     if (error) throw error;
  
  //     // The data returned is a signed URL for the zip file
  //     const downloadUrl = data.downloadUrl;
  
  //     // Create a temporary anchor element and trigger the download
  //     const a = document.createElement('a');
  //     a.style.display = 'none';
  //     a.href = downloadUrl;
  //     a.download = 'all_submissions.zip';
  //     document.body.appendChild(a);
  //     a.click();
  //     document.body.removeChild(a);
  
  //     alert('All submissions downloaded successfully!');
  //   } catch (error) {
  //     console.error('Error downloading all submissions:', error);
  //     setError('Failed to download all submissions. Please try again.');
  //   }
  //   setLoading(false);
  // };

  
  const downloadAllSubmissions = async () => {
    try {
      const { data, error } = await supabase.functions.invoke('download-all-submissions', {
        method: 'POST',
      });

      if (error) throw error;

      const submissions = data.submissions;
      const zip = new JSZip();

      // Download each file and add it to the zip
      await Promise.all(submissions.map(async (submission: { audioUrl: RequestInfo | URL; artist_name: any; song_name: any; artworkUrl: RequestInfo | URL; }) => {
        const audioResponse = await fetch(submission.audioUrl);
        const audioBlob = await audioResponse.blob();
        zip.file(`${submission.artist_name} - ${submission.song_name}.mp3`, audioBlob);

        if (submission.artworkUrl) {
          const artworkResponse = await fetch(submission.artworkUrl);
          const artworkBlob = await artworkResponse.blob();
          zip.file(`${submission.artist_name} - ${submission.song_name}_artwork.jpg`, artworkBlob);
        }
      }));

      // Generate the zip file
      const zipBlob = await zip.generateAsync({ type: 'blob' });

      // Trigger the download
      saveAs(zipBlob, 'all_submissions.zip');

    } catch (error) {
      console.error('Error:', error);
      // Handle the error (e.g., show an error message to the user)
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
      <button type="submit" onClick={downloadAllSubmissions} disabled={loading}>
        DOWNLOAD ALL SUBMISSIONS
      </button>
      <table>
        <thead>
          <tr>
            <th>ARTIST NAME</th>
            <th>SONG NAME</th>
            <th>SUBMISSION DATE</th>
            <th>AUDIO</th>
            <th>ARTWORK</th>
          </tr>
        </thead>
        <tbody>
          {submissions.map((submission) => (
            <tr key={submission.id}>
              <td>{submission.artist_name}</td>
              <td>{submission.song_name}</td>
              <td>{new Date(submission.created_at).toLocaleString()}</td>
              <td>
                <button type="submit" onClick={() => downloadFile(submission.audio_path, `${submission.artist_name} - ${submission.song_name}.mp3`)}>
                  DOWNLOAD
                </button>
              </td>
              <td>
                {submission.art_path ? (
                  <button type="submit" onClick={() => downloadFile(submission.art_path!, `${submission.artist_name} - ${submission.song_name}_artwork.jpg`)}>
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