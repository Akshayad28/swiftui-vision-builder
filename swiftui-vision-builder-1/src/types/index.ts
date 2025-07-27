export interface FileStructure {
    path: string;
    content: string;
}

export interface ProjectStructure {
    projectName: string;
    files: FileStructure[];
}