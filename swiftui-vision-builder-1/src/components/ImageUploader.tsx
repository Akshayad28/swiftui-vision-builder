import React from 'react';

interface ImageUploaderProps {
    files: File[];
    previews: string[];
    onFilesUpdate: (newFiles: File[]) => void;
    customPrompt: string;
    onCustomPromptChange: (prompt: string) => void;
    onGenerate: () => void;
    isGenerating: boolean;
    generationError: string | null;
}

const ImageUploader: React.FC<ImageUploaderProps> = ({
    files,
    previews,
    onFilesUpdate,
    customPrompt,
    onCustomPromptChange,
    onGenerate,
    isGenerating,
    generationError,
}) => {
    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFiles = Array.from(event.target.files || []);
        onFilesUpdate(selectedFiles);
    };

    return (
        <div className="flex flex-col">
            <input
                type="file"
                multiple
                accept="image/*"
                onChange={handleFileChange}
                className="mb-4"
            />
            <div className="flex flex-col mb-4">
                {previews.map((preview, index) => (
                    <img key={index} src={preview} alt={`Preview ${index + 1}`} className="mb-2" />
                ))}
            </div>
            <textarea
                value={customPrompt}
                onChange={(e) => onCustomPromptChange(e.target.value)}
                placeholder="Enter custom prompt here..."
                className="mb-4 p-2 border rounded"
            />
            <button
                onClick={onGenerate}
                disabled={isGenerating}
                className={`p-2 rounded ${isGenerating ? 'bg-gray-400' : 'bg-blue-500 hover:bg-blue-700'} text-white`}
            >
                {isGenerating ? 'Generating...' : 'Generate Project'}
            </button>
            {generationError && <p className="text-red-500 mt-2">{generationError}</p>}
        </div>
    );
};

export default ImageUploader;