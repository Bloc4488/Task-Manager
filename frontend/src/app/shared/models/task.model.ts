export interface Task {
  id: number;
  title: string;
  description: string;
  status: Status;
  userEmail: string;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskRequest {
  title: string;
  description: string;
  status: Status;
  categoryId: number;
}

export enum Status {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE'
}
