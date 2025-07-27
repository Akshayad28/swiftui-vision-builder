// filepath: /swiftui-vision-builder/swiftui-vision-builder/src/types/index.ts
export interface ProjectFile {
    path: string;
    content: string;
}

export interface ProjectStructure {
    projectName: string;
    files: ProjectFile[];
}