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
            <div className="flex flex-wrap">
                {previews.map((preview, index) => (
                    <img
                        key={index}
                        src={preview}
                        alt={`Preview ${index + 1}`}
                        className="w-32 h-32 object-cover m-2 rounded"
                    />
                ))}
            </div>
            <textarea
                value={customPrompt}
                onChange={(e) => onCustomPromptChange(e.target.value)}
                placeholder="Enter custom prompt here..."
                className="mt-4 p-2 border rounded"
            />
            <button
                onClick={onGenerate}
                disabled={isGenerating}
                className="mt-4 p-2 bg-blue-500 text-white rounded"
            >
                {isGenerating ? 'Generating...' : 'Generate Project'}
            </button>
            {generationError && (
                <p className="mt-2 text-red-500">{generationError}</p>
            )}
        </div>
    );
};

export default ImageUploader;