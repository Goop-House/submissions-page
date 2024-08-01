import React from 'react';
import styled from 'styled-components';

const FileInputLabel = styled.label`
  display: inline-block;
  background-color: #9A00F5;
  color: #000;
  padding: 10px 20px;
  margin: 10px 0;
  cursor: pointer;
  font-family: 'Courier New', monospace;
  font-weight: bold;
  text-transform: uppercase;
  transition: all 0.3s ease;

  &:hover {
    background-color: #000;
    color: #9A00F5;
    border: 1px solid #9A00F5;
  }
`;

const HiddenFileInput = styled.input`
  display: none;
`;

interface FileInputProps {
  onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  accept?: string;
  id: string;
}

const FileInput: React.FC<FileInputProps> = ({ onChange, accept, id }) => {
  return (
    <>
      <FileInputLabel htmlFor={id}>
        CHOOSE FILE
      </FileInputLabel>
      <HiddenFileInput
        type="file"
        id={id}
        onChange={onChange}
        accept={accept}
      />
    </>
  );
};

export default FileInput;