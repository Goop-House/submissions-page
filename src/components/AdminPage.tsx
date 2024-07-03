import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';
import styled from 'styled-components';
import firebase from 'firebase/app';
import 'firebase/firestore';
import { Timestamp } from 'firebase/firestore';

const AdminContainer = styled.div`
  background-color: rgba(154, 0, 245, 0.1);
  padding: 20px;
  border: 1px solid #9A00F5;
  border-radius: 5px;
`;

const SubmissionList = styled.ul`
  list-style-type: none;
  padding: 0;
`;

const SubmissionItem = styled.li`
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #9A00F5;
  border-radius: 5px;
`;

const DownloadButton = styled.button`
  background-color: #9A00F5;
  color: #000;
  border: none;
  padding: 10px 20px;
  margin-top: 20px;
  cursor: pointer;
  font-family: 'Courier New', monospace;
  font-weight: bold;

  &:hover {
    background-color: #000;
    color: #9A00F5;
    border: 1px solid #9A00F5;
  }
`;

const SubmissionCount = styled.div`
  margin-top: 20px;
  font-size: 18px;
  font-weight: bold;
`;

interface Submission {
  id: string;
  artistName: string;
  songTitle: string;
  audioUrl: string;
  artUrl?: string;
  timestamp: Timestamp | Date | string;
}

const AdminPage: React.FC = () => {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const navigate = useNavigate();
  const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

  useEffect(() => {
    const checkPermissions = async () => {
      try {
        const userResponse = await axios.get(`${API_BASE_URL}/api/auth/session`, { withCredentials: true });
        const user = userResponse.data;
        if (user && user.id) {
          const uid = user.id;

          const adminResponse = await axios.get(`${API_BASE_URL}/api/admins/${uid}`, { withCredentials: true });
          if (adminResponse.data.exists) {
            setIsAdmin(true);
            const submissionsResponse = await axios.get(`${API_BASE_URL}/api/submissions`, { withCredentials: true });
            const fetchedSubmissions = submissionsResponse.data.map((submission: any) => ({
              ...submission,
              timestamp: parseTimestamp(submission.timestamp),
            }));
            setSubmissions(fetchedSubmissions);
          } else {
            navigate('/login');
          }
        } else {
          navigate('/login');
        }
      } catch (error) {
        console.error('Error checking permissions:', error);
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    checkPermissions();
  }, [navigate, API_BASE_URL]);

  const parseTimestamp = (timestamp: any) => {
    if (timestamp instanceof Date) {
      return timestamp;
    }
    if (timestamp.seconds) {
      return new Date(timestamp.seconds * 1000);
    }
    if (typeof timestamp === 'string') {
      return new Date(timestamp);
    }
    return new Date();
  };

  const handleDownloadAll = async () => {
    const zip = new JSZip();

    for (const submission of submissions) {
      try {
        const audioResponse = await axios.get(`${API_BASE_URL}${submission.audioUrl}`, { responseType: 'blob' });
        zip.file(`${submission.artistName} - ${submission.songTitle}.mp3`, audioResponse.data);

        if (submission.artUrl) {
          const artResponse = await axios.get(`${API_BASE_URL}${submission.artUrl}`, { responseType: 'blob' });
          zip.file(`${submission.artistName} - ${submission.songTitle} (Artwork).jpg`, artResponse.data);
        }
      } catch (error) {
        console.error(`Error downloading ${submission.songTitle}:`, error);
      }
    }

    const content = await zip.generateAsync({ type: 'blob' });
    saveAs(content, 'goop_house_submissions.zip');
  };

  const handleLogout = async () => {
    try {
    //   await axios.post('/api/logout');
      navigate('/login');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAdmin) {
    return <div>Unauthorized</div>;
  }

  return (
    <AdminContainer>
      <h1>GOOP HOUSE ADMIN VOID</h1>
      <div id="login-status">STATUS: ADMIN LOGGED IN</div>
      <button id="login-button" onClick={handleLogout} style={{ display: 'block' }}>
        ESCAPE THE VOID
      </button>
      <DownloadButton onClick={handleDownloadAll}>DOWNLOAD ALL SUBMISSIONS</DownloadButton>
      <SubmissionCount>Total Submissions: {submissions.length}</SubmissionCount>
      <SubmissionList>
        {submissions.map((submission) => (
          <SubmissionItem key={submission.id}>
            {submission.artistName} - {submission.songTitle} (Submitted: {submission.timestamp.toLocaleString()})
          </SubmissionItem>
        ))}
      </SubmissionList>
    </AdminContainer>
  );
};

export default AdminPage;
