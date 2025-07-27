import React from 'react';

export const LogoIcon = ({ className }: { className?: string }) => (
    <svg className={className} viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fillRule="evenodd" clipRule="evenodd" d="M14 0C21.732 0 28 6.26801 28 14C28 21.732 21.732 28 14 28C6.26801 28 0 21.732 0 14C0 6.26801 6.26801 0 14 0ZM15.166 21.0312V16.3437H19.8125C20.5054 16.3437 21.0625 15.7866 21.0625 15.0938V12.875C21.0625 12.1821 20.5054 11.625 19.8125 11.625H15.166V6.96875C15.166 6.55958 14.8302 6.22375 14.421 6.22375H12.2022C11.793 6.22375 11.4572 6.55958 11.4572 6.96875V11.625H6.96875C6.55958 11.625 6.22375 11.9608 6.22375 12.37V14.5888C6.22375 14.998 6.55958 15.3338 6.96875 15.3338H11.4572V21.0312C11.4572 21.4404 11.793 21.7762 12.2022 21.7762H14.421C14.8302 21.7762 15.166 21.4404 15.166 21.0312Z" fill="url(#paint0_linear_1_2)"/>
        <defs>
            <linearGradient id="paint0_linear_1_2" x1="0" y1="0" x2="28" y2="28" gradientUnits="userSpaceOnUse">
                <stop stopColor="#007AFF"/>
                <stop offset="1" stopColor="#5E5CE6"/>
            </linearGradient>
        </defs>
    </svg>
);

export const UploadIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
    </svg>
);

export const CodeIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
    </svg>
);

export const DownloadIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
    </svg>
);

export const CloseIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
);

export const PlusIcon = ({ className }: { className?: string }) => (
  <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
  </svg>
);

export const GripVerticalIcon = ({ className }: { className?: string }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 4h1.5M9 8h1.5M9 12h1.5M9 16h1.5M13.5 4h1.5M13.5 8h1.5M13.5 12h1.5M13.5 16h1.5M9 20h1.5M13.5 20h1.5" />
    </svg>
);
