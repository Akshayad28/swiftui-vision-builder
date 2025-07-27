import React from 'react';
import { LogoIcon } from './icons';

const Header: React.FC = () => {
    return (
        <header className="fixed top-0 left-0 right-0 z-50 transition-all duration-300">
            <div className="mx-auto max-w-screen-2xl px-4 sm:px-6 lg:px-8">
                 <div className="mt-4 flex h-16 items-center justify-between glass-card rounded-2xl px-4 sm:px-6 lg:px-8 shadow-md shadow-gray-200/50">
                    <div className="flex items-center gap-2">
                        <LogoIcon className="h-7 w-7" />
                        <span className="text-lg font-bold text-gray-800">SwiftUI Vision Builder</span>
                    </div>
                    <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-gray-600">
                        <a href="#" className="hover:text-indigo-500 transition-colors">Home</a>
                        <a href="#" className="hover:text-indigo-500 transition-colors">Docs</a>
                        <a href="#" className="hover:text-indigo-500 transition-colors">Feedback</a>
                    </nav>
                </div>
            </div>
        </header>
    );
};

export default Header;
