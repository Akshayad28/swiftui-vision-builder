import React from 'react';
import { ProjectStructure } from '../types';

interface CodePreviewProps {
    project: ProjectStructure | null;
    isLoading: boolean;
    onDownload: () => void;
}

const CodePreview: React.FC<CodePreviewProps> = ({ project, isLoading, onDownload }) => {
    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (!project) {
        return <div>No project generated yet.</div>;
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">Generated Project Structure</h2>
            <h3 className="text-xl font-semibold">{project.projectName}</h3>
            <ul className="list-disc pl-5">
                {project.files.map((file, index) => (
                    <li key={index}>
                        {file.path}
                    </li>
                ))}
            </ul>
            <button 
                onClick={onDownload} 
                className="mt-4 bg-blue-500 text-white py-2 px-4 rounded"
            >
                Download Project
            </button>
        </div>
    );
};

export default CodePreview;