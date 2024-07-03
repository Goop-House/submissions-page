import { createGlobalStyle } from 'styled-components';

export const GlobalStyle = createGlobalStyle`
  body {
    font-family: 'Courier New', monospace;
    background-color: #000;
    color: #9A00F5;
    line-height: 1.6;
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
    text-shadow: 0 0 5px #9A00F5;
  }
  h1 {
    color: #9A00F5;
    text-align: center;
    font-size: 2.5em;
    margin-bottom: 30px;
  }
  form {
    background-color: rgba(154, 0, 245, 0.1);
    padding: 20px;
    border: 1px solid #9A00F5;
    border-radius: 5px;
  }
  label {
    display: block;
    margin-top: 20px;
    font-weight: bold;
  }
  input[type="text"], input[type="file"] {
    width: 100%;
    padding: 10px;
    margin-top: 5px;
    background-color: #000;
    border: 1px solid #9A00F5;
    color: #9A00F5;
    font-family: 'Courier New', monospace;
  }
  input[type="submit"], button {
    background-color: #9A00F5;
    color: #000;
    border: none;
    padding: 10px 20px;
    margin-top: 20px;
    cursor: pointer;
    font-family: 'Courier New', monospace;
    font-weight: bold;
  }
  input[type="submit"]:hover, button:hover {
    background-color: #000;
    color: #9A00F5;
    border: 1px solid #9A00F5;
  }
  .goop {
    font-size: 24px;
    color: #9A00F5;
    text-align: center;
    margin-top: 20px;
    animation: blink 1s infinite;
  }
  @keyframes blink {
    0% { opacity: 0; }
    50% { opacity: 1; }
    100% { opacity: 0; }
  }
  #login-button {
    background-color: #7289da;
    color: #fff;
    border: none;
    padding: 10px 20px;
    cursor: pointer;
    font-family: 'Courier New', monospace;
    margin-bottom: 20px;
    display: block;
    margin: 20px auto;
  }
  #login-status {
    text-align: center;
    margin-bottom: 20px;
  }
  #countdown {
    text-align: center;
    font-size: 18px;
    margin-top: 20px;
  }
`;