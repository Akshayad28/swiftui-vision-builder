import React, { useState, useCallback } from 'react';
import { GoogleGenAI, Type } from "@google/genai";
import { ProjectStructure } from './types';
import CodePreview from './components/CodePreview';
import { DownloadIcon } from './components/icons';
import Header from './components/Header';
import ImageUploader from './components/ImageUploader';

declare const JSZip: any;

const App: React.FC = () => {
    const [files, setFiles] = useState<File[]>([]);
    const [previews, setPreviews] = useState<string[]>([]);
    const [customPrompt, setCustomPrompt] = useState('');
    const [generatedProject, setGeneratedProject] = useState<ProjectStructure | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleFilesUpdate = (newFiles: File[]) => {
        setFiles(newFiles);
        setGeneratedProject(null);
        setError(null);

        if (newFiles.length === 0) {
            setPreviews([]);
            return;
        }

        const newPreviews = newFiles.map(file => URL.createObjectURL(file));
        previews.forEach(p => URL.revokeObjectURL(p));
        setPreviews(newPreviews);
    };
    
    const fileToGenerativePart = (file: File): Promise<{inlineData: {data: string, mimeType: string}}> => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                const base64Data = (reader.result as string).split(',')[1];
                resolve({
                    inlineData: { data: base64Data, mimeType: file.type },
                });
            };
            reader.readAsDataURL(file);
        });
    };

    const createPrompt = (fileCount: number, customInstructions: string) => {
        const singleScreenPrompt = `
Analyze the provided UI image and generate a complete, production-ready SwiftUI Xcode project structure.
The project name should be descriptive and PascalCased, ending in "App" (e.g., "UserProfileApp").
`;

        const multiScreenPrompt = `
Analyze the provided UI images, which represent a sequence of screens in an application. Generate a complete, production-ready SwiftUI Xcode project that includes navigation between these screens. The order of the images corresponds to the primary user flow.

**CRITICAL INSTRUCTIONS:**
1.  **Generate High-Quality SwiftUI:** Produce clean, readable, and idiomatic SwiftUI code. Implement navigation (e.g., using NavigationStack and NavigationLink) to connect the screens in the order they were provided. For interactive elements, add basic @State variables. Include PreviewProviders for each view.
2.  **Create Project Structure:** Organize the code logically. Create separate SwiftUI View files for each screen. Create a main App.swift file that sets up the initial view. A top-level folder should be named after the app.
`;

        return `
${fileCount > 1 ? multiScreenPrompt : singleScreenPrompt}

**COMMON INSTRUCTIONS FOR ANY NUMBER OF SCREENS:**
-   **Analyze & Deconstruct:** Meticulously analyze the layout, components, text, and colors from EACH image.
-   **Component Mapping:** Identify UI elements like Text, Buttons, TextFields, SecureFields, Toggles, Images, and layout containers (VStack, HStack, ZStack).
-   **Styling:** Replicate colors, fonts, and spacing as closely as possible.
-   **Icons:** Map icons to SF Symbols where appropriate.
-   **Strict JSON Output:** You MUST return the entire project structure as a single, valid JSON object that strictly conforms to the provided 'responseSchema'. Do not output any text, markdown, or explanations before or after the JSON object. The response must start with '{' and end with '}'.

${customInstructions ? `**Additional User Instructions:**\n${customInstructions}` : ''}
`;
    };

    const handleGenerateProject = async () => {
        if (files.length === 0) {
            setError("Please upload at least one UI image.");
            return;
        }

        setIsLoading(true);
        setError(null);
        setGeneratedProject(null);

        try {
            const ai = new GoogleGenAI({ apiKey: process.env.API_KEY! });
            
            const imageParts = await Promise.all(files.map(fileToGenerativePart));
            const prompt = createPrompt(files.length, customPrompt);

            const contents = { parts: [{ text: prompt }, ...imageParts] };

            const responseSchema = {
                type: Type.OBJECT,
                properties: {
                    projectName: { type: Type.STRING, description: 'A suitable name for the Xcode project, ending with "App".' },
                    files: {
                        type: Type.ARRAY,
                        description: 'An array of file objects representing the project structure.',
                        items: {
                            type: Type.OBJECT,
                            properties: {
                                path: { type: Type.STRING, description: 'The full path of the file within the project. e.g., "AppName/ContentView.swift".' },
                                content: { type: Type.STRING, description: 'The complete Swift code or content for the file.' }
                            },
                            required: ['path', 'content']
                        }
                    }
                },
                required: ['projectName', 'files']
            };

            const response = await ai.models.generateContent({
                model: 'gemini-2.5-flash',
                contents: contents,
                config: {
                    responseMimeType: 'application/json',
                    responseSchema: responseSchema,
                },
            });
            
            if (!response.text) {
                throw new Error("No response text received from the model.");
            }
            const jsonText = response.text.trim();
            const project = JSON.parse(jsonText) as ProjectStructure;
            setGeneratedProject(project);

        } catch (e) {
            console.error(e);
            setError("Failed to generate project. The model may be overloaded or the image is unprocessable. Please try again with a clearer UI image.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleDownloadProject = async () => {
        if (!generatedProject) return;
        const zip = new JSZip();
        generatedProject.files.forEach(file => {
            zip.file(file.path, file.content);
        });
        const content = await zip.generateAsync({ type: "blob" });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(content);
        link.download = `${generatedProject.projectName}.zip`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    return (
        <div className="min-h-screen w-full p-4 sm:p-6 lg:p-8 flex flex-col items-center animate-fade-in">
            <Header />
            <main className="w-full max-w-screen-2xl mx-auto flex flex-col items-center">
                <div className="text-center my-12 sm:my-16 animate-slide-up">
                    <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-gray-800">
                        SwiftUI <span className="gradient-text">Vision Builder</span>
                    </h1>
                    <p className="mt-4 text-lg text-gray-500 max-w-3xl mx-auto">
                        Transform your UI images into a ready-to-run Xcode project in seconds.
                    </p>
                </div>

                <div className="w-full grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                    <div className="glass-card rounded-2xl p-6 shadow-lg shadow-gray-200/50">
                        <CodePreview 
                            project={generatedProject} 
                            isLoading={isLoading}
                            onDownload={handleDownloadProject}
                        />
                    </div>
                    <div className="glass-card rounded-2xl p-6 shadow-lg shadow-gray-200/50">
                        <ImageUploader 
                           files={files}
                           previews={previews}
                           onFilesUpdate={handleFilesUpdate}
                           customPrompt={customPrompt}
                           onCustomPromptChange={setCustomPrompt}
                           onGenerate={handleGenerateProject}
                           isGenerating={isLoading}
                           generationError={error}
                        />
                    </div>
                </div>
            </main>
        </div>
    );
};

export default App;