import React from 'react';

const Header: React.FC = () => {
    return (
        <header className="w-full py-4 bg-gray-800 text-white text-center">
            <h1 className="text-3xl font-bold">SwiftUI Vision Builder</h1>
            <p className="mt-2 text-lg">Transform your UI images into a ready-to-run Xcode project in seconds.</p>
        </header>
    );
};

export default Header;