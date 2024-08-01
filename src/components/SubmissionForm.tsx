import React, { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';

interface SubmissionFormProps {
  user: any;
  deadline: number;
}

export const SubmissionForm: React.FC<SubmissionFormProps> = ({ user, deadline }) => {
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [artFile, setArtFile] = useState<File | null>(null);
  const [songName, setSongName] = useState('');
  const [artistName, setArtistName] = useState('');
  const [existingSubmission, setExistingSubmission] = useState<any>(null);

  useEffect(() => {
    fetchExistingSubmission();
  }, [user]);

  const fetchExistingSubmission = async () => {
    if (user) {
      const { data, error } = await supabase
        .from('submissions')
        .select('*')
        .eq('user_id', user.id)
        .single();

      if (data && !error) {
        setExistingSubmission(data);
        setSongName(data.song_name);
        setArtistName(data.artist_name);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (new Date().getTime() > deadline) {
      alert('SUBMISSION WINDOW CLOSED. TIME IS A CONSTRUCT.');
      return;
    }

    if (!audioFile && !existingSubmission) {
      alert('AUDIO FILE IS REQUIRED FOR NEW SUBMISSIONS.');
      return;
    }

    let audioPath = existingSubmission?.audio_path;
    let artPath = existingSubmission?.art_path;

    if (audioFile) {
      const audioFileName = `${user.id}/${Date.now()}_${audioFile.name}`;
      const { error: audioError } = await supabase.storage
        .from('audio')
        .upload(audioFileName, audioFile, { upsert: true });

      if (audioError) {
        console.error('Error uploading audio:', audioError);
        return;
      }
      audioPath = audioFileName;
    }

    if (artFile) {
      const artFileName = `${user.id}/${Date.now()}_${artFile.name}`;
      const { error: artError } = await supabase.storage
        .from('artwork')
        .upload(artFileName, artFile, { upsert: true });

      if (artError) {
        console.error('Error uploading artwork:', artError);
      } else {
        artPath = artFileName;
      }
    }

    const submissionData = {
      user_id: user.id,
      song_name: songName,
      artist_name: artistName,
      audio_path: audioPath,
      art_path: artPath,
    };

    let result;
    if (existingSubmission) {
      result = await supabase
        .from('submissions')
        .update(submissionData)
        .eq('id', existingSubmission.id);
    } else {
      result = await supabase
        .from('submissions')
        .insert([submissionData]);
    }

    if (result.error) {
      console.error('Error saving submission:', result.error);
      alert('GOOP REJECTED. TRY AGAIN LATER.');
    } else {
      alert('GOOP RECEIVED. PROCESSING IN ALTERNATE DIMENSION.');
      fetchExistingSubmission(); 
    }
  };

  return (
    <form id="submission-form" onSubmit={handleSubmit}>
      <label htmlFor="audio">AUDIO FILE {!existingSubmission && '(REQUIRED)'}:</label>
      <input
        type="file"
        id="audio"
        name="audio"
        accept="audio/*"
        onChange={(e) => setAudioFile(e.target.files?.[0] || null)}
      />

      <label htmlFor="art">ARTWORK (OPTIONAL):</label>
      <input
        type="file"
        id="art"
        name="art"
        accept="image/*"
        onChange={(e) => setArtFile(e.target.files?.[0] || null)}
      />

      <label htmlFor="song_name">SONG NAME:</label>
      <input
        type="text"
        id="song_name"
        name="song_name"
        required
        value={songName}
        onChange={(e) => setSongName(e.target.value)}
      />

      <label htmlFor="artist_name">ARTIST NAME:</label>
      <input
        type="text"
        id="artist_name"
        name="artist_name"
        required
        value={artistName}
        onChange={(e) => setArtistName(e.target.value)}
      />

      <input type="submit" value={existingSubmission ? "UPDATE THE VOID" : "SUBMIT TO THE VOID"} />
    </form>
  );
};