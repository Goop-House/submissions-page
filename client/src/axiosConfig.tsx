import axios from 'axios';

const instance = axios.create({
  baseURL: process.env.NODE_ENV === 'production' ? 'https://submit.goop.house' : 'http://localhost:5001',
  withCredentials: true
});

export default instance;