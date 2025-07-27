import React, { useState, useCallback, useRef } from 'react';
import { UploadIcon, CloseIcon, PlusIcon, GripVerticalIcon } from './icons';

interface ImageUploaderProps {
    files: File[];
    previews: string[];
    onFilesUpdate: (files: File[]) => void;
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
    generationError
}) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const dragItem = useRef<number | null>(null);
    const dragOverItem = useRef<number | null>(null);

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const newFiles = Array.from(e.target.files);
            onFilesUpdate([...files, ...newFiles]);
        }
    };

    const handleDrag = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
    }, []);

    const handleDragIn = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.dataTransfer.items && e.dataTransfer.items.length > 0 && !isGenerating) {
            setIsDraggingOver(true);
        }
    }, [isGenerating]);
    
    const handleDragOut = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDraggingOver(false);
    }, []);

    const handleDrop = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDraggingOver(false);
        if (isGenerating) return;
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            const newFiles = Array.from(e.dataTransfer.files);
            onFilesUpdate([...files, ...newFiles]);
            e.dataTransfer.clearData();
        }
    }, [files, onFilesUpdate, isGenerating]);

    const handleRemoveFile = (indexToRemove: number) => {
        const updatedFiles = files.filter((_, index) => index !== indexToRemove);
        onFilesUpdate(updatedFiles);
    };

    const handleDragSort = () => {
        if (dragItem.current === null || dragOverItem.current === null || isGenerating) return;
        const newFiles = [...files];
        const draggedItemContent = newFiles.splice(dragItem.current, 1)[0];
        newFiles.splice(dragOverItem.current, 0, draggedItemContent);
        dragItem.current = null;
        dragOverItem.current = null;
        onFilesUpdate(newFiles);
    };

    const triggerFileSelect = () => fileInputRef.current?.click();

    return (
        <div className="flex flex-col h-full">
             <h2 className="text-xl font-bold text-gray-800 pb-4">Upload & Customize</h2>
             <div className="flex-grow flex flex-col gap-4">
                <div 
                    className={`relative w-full flex-grow border-2 border-dashed rounded-xl transition-all duration-300 flex justify-center items-center ${isDraggingOver ? 'border-indigo-400 bg-indigo-50' : 'border-gray-300 hover:border-gray-400'}`}
                    onDragEnter={handleDragIn} onDragLeave={handleDragOut} onDragOver={handleDrag} onDrop={handleDrop}
                >
                     <input type="file" ref={fileInputRef} onChange={handleFileSelect} accept="image/png, image/jpeg, image/webp" className="hidden" multiple disabled={isGenerating} />
                    {previews.length > 0 ? (
                       <div className="w-full h-full p-4 overflow-y-auto max-h-[400px]">
                           <div className="grid grid-cols-2 gap-4">
                            {previews.map((preview, index) => (
                                <div 
                                    key={index}
                                    className={`relative group aspect-w-16 aspect-h-10 bg-gray-100 rounded-lg ${!isGenerating ? 'cursor-grab active:cursor-grabbing' : 'cursor-default'}`}
                                    draggable={!isGenerating}
                                    onDragStart={() => dragItem.current = index}
                                    onDragEnter={() => dragOverItem.current = index}
                                    onDragEnd={handleDragSort}
                                    onDragOver={(e) => e.preventDefault()}
                                >
                                    <img src={preview} alt={`preview ${index + 1}`} className="w-full h-full object-cover rounded-lg shadow-sm" />
                                    <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity rounded-lg"/>
                                    <span className="absolute top-2 left-2 bg-black/50 text-white text-xs font-bold rounded-full h-6 w-6 flex items-center justify-center">{index + 1}</span>
                                    {!isGenerating && <button onClick={() => handleRemoveFile(index)} className="absolute top-2 right-2 p-1 bg-black/50 rounded-full text-white/80 hover:bg-red-500 hover:text-white transition-all scale-0 group-hover:scale-100">
                                        <CloseIcon className="w-4 h-4" />
                                    </button>}
                                    {!isGenerating && <div className="absolute bottom-2 right-2 text-white/70 scale-0 group-hover:scale-100 transition-transform">
                                        <GripVerticalIcon className="w-5 h-5" />
                                    </div>}
                                </div>
                            ))}
                            </div>
                        </div>
                    ) : (
                        <div onClick={!isGenerating ? triggerFileSelect : undefined} className={`text-center text-gray-500 p-8 ${!isGenerating ? 'cursor-pointer' : 'cursor-not-allowed'}`}>
                            <UploadIcon className="w-12 h-12 mx-auto mb-4 text-gray-400" />
                            <p className="font-semibold">Drag & drop your UI images here</p>
                            <p className="text-sm">or click to browse</p>
                        </div>
                    )}
                </div>
                 <button onClick={triggerFileSelect} disabled={isGenerating} className="w-full flex items-center justify-center gap-2 bg-gray-200/80 hover:bg-gray-300/80 text-gray-600 font-semibold py-3 px-4 rounded-xl transition-colors transform active:scale-95 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed">
                    <PlusIcon className="w-5 h-5"/>
                    Add More Screens
                </button>
                 <div>
                    <label htmlFor="custom-prompt" className="text-sm font-medium text-gray-600">Add custom logic (optional)</label>
                    <textarea 
                        id="custom-prompt"
                        value={customPrompt}
                        onChange={(e) => onCustomPromptChange(e.target.value)}
                        placeholder="e.g., 'Connect the login button to the profile screen.'"
                        className="mt-1 w-full p-3 rounded-xl border border-gray-300 focus:ring-2 focus:ring-indigo-400 focus:border-indigo-400 transition-shadow duration-200 text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
                        rows={3}
                        disabled={isGenerating}
                    />
                 </div>
            </div>
            <div className="mt-6 w-full flex flex-col items-center gap-4">
                 <button
                    onClick={onGenerate}
                    disabled={files.length === 0 || isGenerating}
                    className="w-full max-w-md bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 disabled:from-gray-400 disabled:to-gray-500 disabled:cursor-not-allowed text-white font-bold text-lg py-4 px-8 rounded-full transition-all duration-300 transform hover:scale-105 active:scale-95 focus:outline-none focus:ring-4 focus:ring-indigo-300 shadow-xl shadow-indigo-200/50 disabled:shadow-none flex items-center justify-center gap-3"
                >
                    {isGenerating ? (
                        <>
                            <svg className="animate-spin h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            <span>Generating Project...</span>
                        </>
                    ) : (
                        'Generate SwiftUI App'
                    )}
                </button>
                {generationError && (
                    <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded-xl text-center w-full max-w-md" role="alert">
                        <p className="font-bold text-sm">Error</p>
                        <p className="text-xs">{generationError}</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ImageUploader;