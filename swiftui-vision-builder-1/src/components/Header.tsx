import React from 'react';

const Header: React.FC = () => {
    return (
        <header className="w-full py-4 bg-gray-100 shadow">
            <div className="max-w-screen-2xl mx-auto text-center">
                <h1 className="text-3xl font-bold text-gray-800">SwiftUI Vision Builder</h1>
                <p className="mt-2 text-lg text-gray-600">
                    Transform your UI images into a ready-to-run Xcode project in seconds.
                </p>
            </div>
        </header>
    );
};

export default Header;