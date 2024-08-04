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
  const [artistName4, setArtistName4] = useState('');
  const [existingSubmission, setExistingSubmission] = useState<any>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [totalLengthError, setTotalLengthError] = useState('');
  const [emojiError, setEmojiError] = useState('');
  const projectId = 'qpovypmwxnwjaucsfujx';

  const MAX_AUDIO_FILE_SIZE = 1000 * 1024 * 1024;
  const MAX_ART_FILE_SIZE = 30 * 1024 * 1024;
  const MAX_COMBINED_LENGTH = 90;

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
        setArtistName4(data.artist_name4 || '');
      }
    }
  };

  const uploadFile = async (file: File, bucketName: string, fileName: string) => {
    if (bucketName === 'audio' && file.size > MAX_AUDIO_FILE_SIZE) {
      alert('File size exceeds the maximum limit.');
      return null;
    }
    if (bucketName === 'artwork' && file.size > MAX_ART_FILE_SIZE) {
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

  const handleChange = (setter: React.Dispatch<React.SetStateAction<string>>, field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (containsEmoji(value)) {
      setEmojiError('Emojis are not allowed.');
    } else {
      setEmojiError('');
      setter(value);
      validateTotalLength(field, value);
    }
  };

  const containsEmoji = (text: string) => {
    const emojiRegex = /[\uD83C-\uDBFF\uDC00-\uDFFF]+/g;
    return emojiRegex.test(text);
  };

  const validateTotalLength = (field: string, value: string) => {
    const lengths = {
      songName: songName.length,
      artistName1: artistName1.length,
      artistName2: artistName2.length,
      artistName3: artistName3.length,
      artistName4: artistName4.length,
      [field]: value.length
    };
    const totalLength = lengths.songName + lengths.artistName1 + lengths.artistName2 + lengths.artistName3 + lengths.artistName4;
    if (totalLength > MAX_COMBINED_LENGTH) {
      setTotalLengthError(`Combined length of all fields cannot exceed ${MAX_COMBINED_LENGTH} characters.`);
    } else {
      setTotalLengthError('');
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

    if (totalLengthError || emojiError) {
      alert(totalLengthError || emojiError);
      return;
    }

    // Additional file type checks
    const validAudioTypes = ['audio/mpeg', 'audio/wav', 'audio/mp3'];
    const validArtTypes = ['image/jpeg', 'image/png', 'image/gif'];

    if (audioFile && !validAudioTypes.includes(audioFile.type)) {
      alert('Invalid audio file type.');
      return;
    }

    if (artFile && !validArtTypes.includes(artFile.type)) {
      alert('Invalid artwork file type.');
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
      artist_name4: artistName4,
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
      fetchExistingSubmission();
      // Clear the file inputs after submission
      setAudioFile(null);
      setArtFile(null);
      (document.getElementById('audio') as HTMLInputElement).value = '';
      (document.getElementById('art') as HTMLInputElement).value = '';

      alert('GOOP RECEIVED. PROCESSING IN ALTERNATE DIMENSION.');
    }
  };

  return (
    <form id="submission-form" onSubmit={handleSubmit}>
      <label htmlFor="audio">AUDIO FILE {!existingSubmission && '(REQUIRED)'}:</label>
      <input
        type="file"
        id="audio"
        name="audio"
        accept="audio/mpeg, audio/wav, audio/mp3" // Restricting to common audio MIME types
        onChange={(e) => setAudioFile(e.target.files?.[0] || null)}
      />

      <label htmlFor="art">ARTWORK (OPTIONAL):</label>
      <input
        type="file"
        id="art"
        name="art"
        accept="image/jpeg, image/png, image/gif" // Restricting to common image MIME types
        onChange={(e) => setArtFile(e.target.files?.[0] || null)}
      />

      <label htmlFor="song_name">SONG NAME:</label>
      <input
        type="text"
        id="song_name"
        name="song_name"
        maxLength={254}
        required
        value={songName}
        onChange={handleChange(setSongName, 'songName')}
      />

      <label htmlFor="artist_name1">ARTIST NAME 1:</label>
      <input
        type="text"
        id="artist_name1"
        name="artist_name1"
        maxLength={254}
        required
        value={artistName1}
        onChange={handleChange(setArtistName1, 'artistName1')}
      />

      <label htmlFor="artist_name2">ARTIST NAME 2:</label>
      <input
        type="text"
        id="artist_name2"
        name="artist_name2"
        maxLength={254}
        value={artistName2}
        onChange={handleChange(setArtistName2, 'artistName2')}
      />

      <label htmlFor="artist_name3">ARTIST NAME 3:</label>
      <input
        type="text"
        id="artist_name3"
        name="artist_name3"
        maxLength={254}
        value={artistName3}
        onChange={handleChange(setArtistName3, 'artistName3')}
      />

      <label htmlFor="artist_name4">ARTIST NAME 4:</label>
      <input
        type="text"
        id="artist_name4"
        name="artist_name4"
        maxLength={254}
        value={artistName4}
        onChange={handleChange(setArtistName4, 'artistName4')}
      />

      {totalLengthError && <p style={{ color: 'red' }}>{totalLengthError}</p>}
      {emojiError && <p style={{ color: 'red' }}>{emojiError}</p>}

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
