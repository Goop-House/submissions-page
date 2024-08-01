import React, { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { Upload } from 'tus-js-client';

interface SubmissionFormProps {
  user: any;
  deadline: number;
}

export const SubmissionForm: React.FC<SubmissionFormProps> = ({ user, deadline }) => {
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [artFile, setArtFile] = useState<File | null>(null);
  const [songName, setSongName] = useState('');
  const [artistName1, setArtistName1] = useState('');
  const [artistName2, setArtistName2] = useState('');
  const [artistName3, setArtistName3] = useState('');
  const [existingSubmission, setExistingSubmission] = useState<any>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const projectId = 'qpovypmwxnwjaucsfujx';

  const MAX_FILE_SIZE = 5 * 1024 * 1024 * 1024; // 5GB

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
        setArtistName1(data.artist_name1 || '');
        setArtistName2(data.artist_name2 || '');
        setArtistName3(data.artist_name3 || '');
      }
    }
  };

  const uploadFile = async (file: File, bucketName: string, fileName: string) => {
    if (file.size > MAX_FILE_SIZE) {
      alert('File size exceeds the maximum limit.');
      return null;
    }
  
    const { data: { session } } = await supabase.auth.getSession();
    setIsUploading(true);
  
    return new Promise((resolve, reject) => {
      const upload = new Upload(file, {
        endpoint: `https://${projectId}.supabase.co/storage/v1/upload/resumable`,
        retryDelays: [0, 3000, 5000, 10000, 20000],
        headers: {
          authorization: `Bearer ${session?.access_token}`,
          'x-upsert': 'true',
        },
        uploadDataDuringCreation: true,
        removeFingerprintOnSuccess: true,
        metadata: {
          bucketName: bucketName,
          objectName: fileName,
          contentType: file.type,
          cacheControl: '3600',
        },
        chunkSize: 6 * 1024 * 1024, // 6MB
        onError: function (error) {
          console.log('Failed because: ' + error);
          reject(error);
        },
        onProgress: function (bytesUploaded, bytesTotal) {
          const percentage = ((bytesUploaded / bytesTotal) * 100).toFixed(2);
          setUploadProgress(Number(percentage));
        },
        onSuccess: function () {
          console.log('Download %s from %s', (upload.file as File).name, upload.url);
          resolve(fileName);
        },
      });
  
      upload.findPreviousUploads().then(function (previousUploads) {
        if (previousUploads.length) {
          upload.resumeFromPreviousUpload(previousUploads[0]);
        }
        upload.start();
      });
    }).finally(() => {
      setIsUploading(false);
    });
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
      audioPath = await uploadFile(audioFile, 'audio', audioFileName);
      if (!audioPath) return;
    }
  
    if (artFile) {
      const artFileName = `${user.id}/${Date.now()}_${artFile.name}`;
      artPath = await uploadFile(artFile, 'artwork', artFileName);
      if (!artPath) return;
    }
  
    const submissionData = {
      user_id: user.id,
      song_name: songName,
      artist_name1: artistName1,
      artist_name2: artistName2,
      artist_name3: artistName3,
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

      <label htmlFor="artist_name1">ARTIST NAME 1:</label>
      <input
        type="text"
        id="artist_name1"
        name="artist_name1"
        required
        value={artistName1}
        onChange={(e) => setArtistName1(e.target.value)}
      />

      <label htmlFor="artist_name2">ARTIST NAME 2:</label>
      <input
        type="text"
        id="artist_name2"
        name="artist_name2"
        value={artistName2}
        onChange={(e) => setArtistName2(e.target.value)}
      />

      <label htmlFor="artist_name3">ARTIST NAME 3:</label>
      <input
        type="text"
        id="artist_name3"
        name="artist_name3"
        value={artistName3}
        onChange={(e) => setArtistName3(e.target.value)}
      />

      <input type="submit" value={existingSubmission ? "UPDATE THE VOID" : "SUBMIT TO THE VOID"} />

      {isUploading && (
        <div>
          <progress value={uploadProgress} max="100">{uploadProgress}%</progress>
          <p>Uploading... {Math.round(uploadProgress)}%</p>
        </div>
      )}
    </form>
  );
};
