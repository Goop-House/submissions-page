// src/axiosConfig.ts
import axios from 'axios';

const instance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'https://submit.goop.house',
  withCredentials: true
});

// Request interceptor
instance.interceptors.request.use(
  (config) => {
    console.log('Outgoing request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
instance.interceptors.response.use(
  (response) => {
    console.log('Incoming response:', response.status, response.config.url);
    return response;
  },
  (error) => {
    if (axios.isAxiosError(error)) {
      console.error('Response error:', error.response?.status, error.response?.data);
    } else {
      console.error('Unknown error:', error);
    }
    return Promise.reject(error);
  }
);

export default instance;