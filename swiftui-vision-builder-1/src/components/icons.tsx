import React from 'react';

export const DownloadIcon: React.FC<React.SVGProps<SVGSVGElement>> = (props) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
        {...props}
    >
        <path d="M12 3v10.586l-4.293-4.293-1.414 1.414L12 16.414l5.707-5.707-1.414-1.414L12 13.586V3z" />
        <path d="M4 17h16v2H4v-2z" />
    </svg>
);

export const UploadIcon: React.FC<React.SVGProps<SVGSVGElement>> = (props) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
        {...props}
    >
        <path d="M12 3v10.586l4.293-4.293 1.414 1.414L12 16.414l-5.707-5.707 1.414-1.414L12 13.586V3z" />
        <path d="M4 17h16v2H4v-2z" />
    </svg>
);

// Add more icons as needed.