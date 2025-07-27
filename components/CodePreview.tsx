import React, { useState, useEffect } from 'react';
import { ProjectStructure } from '../types';
import { CodeIcon, DownloadIcon } from './icons';
import Shimmer from './Shimmer';

// Declare Prism on the window object
declare const Prism: any;

interface CodePreviewProps {
    project: ProjectStructure | null;
    isLoading: boolean;
    onDownload: () => void;
}

const CodePreview: React.FC<CodePreviewProps> = ({ project, isLoading, onDownload }) => {
    const files = project?.files ?? [];
    const [selectedFile, setSelectedFile] = useState<typeof files[0] | null>(null);
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        if (files.length > 0) {
            const viewFile = files.find(f => f.path.toLowerCase().includes('view.swift')) || files[0];
            setSelectedFile(viewFile);
        } else {
            setSelectedFile(null);
        }
    }, [project]);

    useEffect(() => {
        if (selectedFile) {
            // Use a timeout to ensure the DOM is updated before highlighting
            setTimeout(() => Prism.highlightAll(), 0);
        }
    }, [selectedFile, selectedFile?.content]);

    const handleCopy = () => {
        if (selectedFile) {
            navigator.clipboard.writeText(selectedFile.content).then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 2000);
            });
        }
    };

    const renderContent = () => {
        if (isLoading) {
            return <Shimmer />;
        }
        
        if (files.length === 0) {
            return (
                <div className="flex flex-col justify-center items-center h-full p-6 text-center text-gray-400">
                    <CodeIcon className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                    <p className="font-semibold text-gray-500">Your generated project will appear here.</p>
                    <p className="text-sm">Upload an image and click "Generate" to start.</p>
                </div>
            );
        }

        return (
            <div className="flex h-full min-h-[500px] lg:min-h-[600px] bg-gray-800 rounded-b-xl overflow-hidden">
                <aside className="w-1/3 max-w-[250px] bg-gray-800/60 p-3 overflow-y-auto">
                    <h3 className="text-xs font-semibold text-gray-400 mb-3 px-2 uppercase tracking-wider">Project Files</h3>
                    <ul>
                        {files.map((file) => (
                            <li key={file.path}>
                                <button
                                    onClick={() => setSelectedFile(file)}
                                    className={`w-full text-left text-sm p-2 rounded-md transition-colors truncate ${
                                        selectedFile?.path === file.path
                                            ? 'bg-indigo-500 text-white font-semibold'
                                            : 'text-gray-300 hover:bg-gray-700/50 hover:text-white'
                                    }`}
                                    title={file.path}
                                >
                                    {file.path.split('/').pop()}
                                </button>
                            </li>
                        ))}
                    </ul>
                </aside>

                <main className="w-2/3 flex-grow flex flex-col bg-[#1e1e1e]">
                    {selectedFile ? (
                        <>
                            <div className="flex justify-between items-center p-3 bg-gray-900/50 border-b border-gray-700/50 text-gray-400">
                                <span className="text-xs font-mono truncate" title={selectedFile.path}>{selectedFile.path}</span>
                                <button
                                    onClick={handleCopy}
                                    className="px-3 py-1 text-xs font-semibold bg-gray-600 hover:bg-gray-500 text-white rounded-md transition-colors"
                                >
                                    {copied ? 'Copied!' : 'Copy'}
                                </button>
                            </div>
                            <div className="relative flex-grow overflow-auto">
                               <pre className="h-full w-full !m-0 !p-0">
                                   <code className="language-swift !p-4 block whitespace-pre-wrap break-words">
                                       {selectedFile.content}
                                   </code>
                               </pre>
                            </div>
                        </>
                    ) : (
                         <div className="flex-grow flex items-center justify-center text-gray-500">
                            <p>Select a file to view</p>
                        </div>
                    )}
                </main>
            </div>
        );
    };

    return (
        <div className="flex flex-col h-full">
            <header className="flex justify-between items-center pb-4">
                <h2 className="text-xl font-bold text-gray-800">Code Preview</h2>
                <button
                    onClick={onDownload}
                    disabled={!project || isLoading}
                    className="bg-green-500 hover:bg-green-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-semibold py-2 px-4 rounded-full transition-all flex items-center gap-2 text-sm transform active:scale-95"
                >
                    <DownloadIcon className="w-5 h-5" />
                    Download .zip
                </button>
            </header>
            <div className="flex-grow bg-gray-200 rounded-xl overflow-hidden ring-1 ring-gray-300/50">
                {renderContent()}
            </div>
             {project && !isLoading && (
                <div className="flex items-center justify-center gap-4 text-xs text-gray-400 pt-4">
                    <span className="flex items-center gap-1">✅ iOS</span>
                    <span className="flex items-center gap-1">✅ SwiftUI</span>
                    <span className="flex items-center gap-1">✅ Xcode 16+</span>
                </div>
            )}
        </div>
    );
};

export default CodePreview;
