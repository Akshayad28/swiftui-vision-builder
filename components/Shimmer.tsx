import React from 'react';

const Shimmer: React.FC = () => {
    return (
        <div className="w-full h-full p-4 bg-gray-200 rounded-b-xl overflow-hidden">
            <div className="animate-pulse flex h-full">
                {/* Sidebar Shimmer */}
                <div className="w-1/3 max-w-[250px] pr-4">
                    <div className="h-4 bg-gray-300 rounded w-1/3 mb-6"></div>
                    <div className="space-y-3">
                        <div className="h-6 bg-gray-300 rounded w-full"></div>
                        <div className="h-6 bg-gray-400 rounded w-3/4"></div>
                        <div className="h-6 bg-gray-300 rounded w-full"></div>
                        <div className="h-6 bg-gray-300 rounded w-5/6"></div>
                    </div>
                </div>

                {/* Main Code Area Shimmer */}
                <div className="flex-1 pl-4 border-l border-gray-300">
                     <div className="h-4 bg-gray-300 rounded w-1/2 mb-6"></div>
                     <div className="space-y-2">
                        <div className="h-4 bg-gray-400 rounded w-3/4"></div>
                        <div className="h-4 bg-gray-300 rounded w-5/6"></div>
                        <div className="h-4 bg-gray-400 rounded w-1/2"></div>
                        <div className="h-4 bg-gray-300 rounded w-full"></div>
                        <div className="h-4 bg-gray-400 rounded w-2/3"></div>
                        <div className="h-4 bg-gray-300 rounded w-5/6"></div>
                        <div className="h-4 bg-gray-400 rounded w-3/4"></div>
                     </div>
                </div>
            </div>
        </div>
    );
};

export default Shimmer;
