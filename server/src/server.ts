//server.ts
import dotenv from 'dotenv';
import express from 'express';
import session from 'express-session';
import cors from 'cors';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import axios from 'axios';
import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';


// Declare module augmentation for express-session
declare module 'express-session' {
  interface SessionData {
    user?: {
      id: string;
      username: string;
      avatar: string;
    };
  }
}

dotenv.config();

// Initialize Firebase Admin
const firebaseApp = initializeApp({
  credential: cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
    privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
  }),
});

const db = getFirestore(firebaseApp);

const app = express();

app.use(express.static(path.join(__dirname, '../../client/build')));


// Session middleware
app.use(session({
  secret: process.env.SESSION_SECRET || 'your_session_secret',
  resave: false,
  saveUninitialized: false,
  cookie: { 
    secure: process.env.NODE_ENV === 'production',
    sameSite: process.env.NODE_ENV === 'production' ? 'none' : 'lax',
    httpOnly: true
  }
}));

// CORS middleware
const allowedOrigins = ['http://localhost:3000'];
app.use(cors({
  origin: process.env.REACT_APP_BASE_URL || 'https://submit.goop.house',
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(express.json());

// Multer setup for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, '../../uploads');
    fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    cb(null, `${Date.now()}-${file.originalname}`);
  }
});

const upload = multer({ storage: storage });

// Serve static files from the uploads directory
app.use('/uploads', express.static(path.join(__dirname, '../../uploads')));

// Auth routes
app.get('/auth/discord', (req, res) => {
  res.redirect(`https://discord.com/api/oauth2/authorize?client_id=${process.env.DISCORD_CLIENT_ID}&redirect_uri=${process.env.DISCORD_REDIRECT_URI}&response_type=code&scope=identify`);
});

app.get('/api/auth/session', (req, res) => {
    if (req.session.user) {
        res.json(req.session.user);
    } else {
        res.status(401).json({ error: 'Not authenticated' });
    }
});

// app.use((req, res, next) => {
//     console.log('Session:', req.session);
//     next();
// });
  
  // Endpoint to check if user is an admin
app.get('/api/admins/:uid', async (req, res) => {
    const uid = req.params.uid;
    const adminDoc = await db.collection('admins').doc(uid).get();
    res.json({ exists: adminDoc.exists });
});

app.get('/auth/discord/callback', async (req, res) => {
  const { code } = req.query;
  
  try {
    const tokenResponse = await axios.post('https://discord.com/api/oauth2/token', 
      new URLSearchParams({
        client_id: process.env.DISCORD_CLIENT_ID!,
        client_secret: process.env.DISCORD_CLIENT_SECRET!,
        code: code as string,
        grant_type: 'authorization_code',
        redirect_uri: process.env.DISCORD_REDIRECT_URI!,
        scope: 'identify',
      }),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      }
    );

    const { access_token } = tokenResponse.data;

    const userResponse = await axios.get('https://discord.com/api/users/@me', {
      headers: {
        authorization: `Bearer ${access_token}`,
      },
    });

    const { id, username, avatar } = userResponse.data;

    // Store user info in session
    req.session.user = { id, username, avatar };
    req.session.save((err) => {
      if (err) {
        console.error('Error saving session:', err);
        return res.status(500).send('Authentication failed');
      }
      res.redirect(process.env.REACT_APP_BASE_URL || 'https://submit.goop.house');
    });
  } catch (error) {
    console.error('Error during Discord authentication:', error);
    res.status(500).send('Authentication failed');
  }
});

// API routes
app.get('/api/user', (req, res) => {
  if (req.session.user) {
    res.json(req.session.user);
  } else {
    res.status(401).json({ error: 'Not authenticated' });
  }
});

app.post('/api/logout', (req, res) => {
  req.session.destroy((err) => {
    if (err) {
      console.error('Error destroying session:', err);
      res.status(500).json({ error: 'Logout failed' });
    } else {
      res.json({ message: 'Logged out successfully' });
    }
  });
});

app.get('/api/deadline', async (req, res) => {
  try {
    const deadlineDoc = await db.collection('config').doc('deadline').get();
    if (!deadlineDoc.exists) {
      res.status(404).json({ error: 'Deadline not set' });
    } else {
      const deadlineData = deadlineDoc.data();
      res.json({ deadline: deadlineData?.timestamp.toDate() });
    }
  } catch (error) {
    console.error('Error fetching deadline:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Submission routes
app.get('/api/submission', async (req, res) => {
  if (!req.session.user) {
    return res.status(401).json({ error: 'Not authenticated' });
  }
  
  try {
    const submissionDoc = await db.collection('submissions').doc(req.session.user.id).get();
    
    if (!submissionDoc.exists) {
      return res.json(null);
    }
    
    const submissionData = submissionDoc.data();
    res.json(submissionData);
  } catch (error) {
    console.error('Error fetching submission:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

//get all submissions
app.get('/api/submissions', async (req, res) => {
  if (!req.session.user) {
    return res.status(401).json({ error: 'Not authenticated' });
  }

  try {
    const submissions = await db.collection('submissions').get();
    const submissionData = submissions.docs.map((doc) => doc.data());
    res.json(submissionData);
  } catch (error) {
    console.error('Error fetching submissions:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/api/submit', upload.fields([
  { name: 'audio', maxCount: 1 },
  { name: 'art', maxCount: 1 }
]), async (req, res) => {
  if (!req.session.user) {
    return res.status(401).json({ error: 'Not authenticated' });
  }

  const { artistName, songTitle } = req.body;
  const files = req.files as { [fieldname: string]: Express.Multer.File[] };
  
  try {
    let audioUrl = '';
    let artUrl = '';

    if (files.audio) {
      audioUrl = `/uploads/${files.audio[0].filename}`;
    }

    if (files.art) {
      artUrl = `/uploads/${files.art[0].filename}`;
    }

    const submission = {
      userId: req.session.user.id,
      artistName,
      songTitle,
      audioUrl,
      artUrl,
      timestamp: Timestamp.now()
    };

    await db.collection('submissions').doc(req.session.user.id).set(submission, { merge: true });

    res.json(submission);
  } catch (error) {
    console.error('Error submitting:', error);
    res.status(500).json({ error: 'Submission failed' });
  }
});

app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../../client/build/index.html'));
});

const PORT = process.env.PORT || 5001;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));