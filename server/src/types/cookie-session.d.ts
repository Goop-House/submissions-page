import 'cookie-session';

declare module 'cookie-session' {
  interface SessionData {
    user?: {
      id: string;
      username: string;
      avatar: string;
    };
  }
}