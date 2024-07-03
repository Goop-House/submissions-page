import React from 'react';
import styled from 'styled-components';

const FooterContainer = styled.footer`
  margin-top: 40px;
  text-align: center;

  ul {
    list-style-type: none;
    padding: 0;
  }

  a {
    color: #9A00F5;
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }
`;

const Footer: React.FC = () => {
  return (
    <FooterContainer>
      <p>NAVIGATE THE GOOP:</p>
      <ul>
        <li><a href="https://www.twitch.tv/goop_house">TWITCH</a></li>
        <li><a href="https://discord.gg/qD2wbqqDGX">DISCORD</a></li>
        <li><a href="https://soundcloud.com/goophouse">MUSIC</a></li>
        <li><a href="https://my-store-bd8e00.creator-spring.com/">ARTIFACTS</a></li>
      </ul>
    </FooterContainer>
  );
};

export default Footer;