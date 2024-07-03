import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

// Set base URL for Axios requests
axios.defaults.baseURL = 'https://submit.goop.house';
axios.defaults.withCredentials = true;  // Ensure credentials are included

const CheckPermissions: React.FC = () => {
  const [userUid, setUserUid] = useState<string | null>(null);
  const [permissions, setPermissions] = useState<any>({});
  const navigate = useNavigate();

  useEffect(() => {
    const checkPermissions = async () => {
      try {
        // Fetch the current user session
        const userResponse = await axios.get('/api/auth/session');
        const user = userResponse.data;
        if (user && user.id) {
          const uid = user.id;
          setUserUid(uid);
          console.log(`User UID: ${uid}`);

          // Check if user is in the admins collection
          const adminResponse = await axios.get(`/api/admins/${uid}`);
          const isAdmin = adminResponse.data.exists;
          console.log(`Admin permission: ${isAdmin}`);
          setPermissions((prev: any) => ({ ...prev, isAdmin }));

          // Check if user can read their own submissions
          const submissionsResponse = await axios.get('/api/submissions');
          const userCanReadSubmissions = submissionsResponse.status === 200;
          console.log(`Can read own submissions: ${userCanReadSubmissions}`);
          setPermissions((prev: any) => ({ ...prev, userCanReadSubmissions }));

          // Attempt to read a config document
          const configResponse = await axios.get('/api/config/deadline');
          const canReadConfig = configResponse.status === 200;
          console.log(`Can read config: ${canReadConfig}`);
          setPermissions((prev: any) => ({ ...prev, canReadConfig }));
        } else {
          console.log('No user is logged in');
          navigate('/login');
        }
      } catch (error) {
        console.error('Error checking permissions:', error);
        navigate('/login');
      }
    };

    checkPermissions();
  }, [navigate]);

  return (
    <div>
      <h2>User Permissions</h2>
      {userUid ? (
        <div>
          <p>User UID: {userUid}</p>
          <p>Admin Permission: {permissions.isAdmin ? 'Yes' : 'No'}</p>
          <p>Can Read Own Submissions: {permissions.userCanReadSubmissions ? 'Yes' : 'No'}</p>
          <p>Can Read Config: {permissions.canReadConfig ? 'Yes' : 'No'}</p>
        </div>
      ) : (
        <p>Loading user permissions...</p>
      )}
    </div>
  );
};

export default CheckPermissions;
