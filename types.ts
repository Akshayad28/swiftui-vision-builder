
export interface GeneratedFile {
  path: string;
  content: string;
}

export interface ProjectStructure {
  projectName: string;
  files: GeneratedFile[];
}
