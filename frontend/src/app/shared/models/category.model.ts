export interface Category {
  id: number;
  name: string;
  description: string;
  userEmail: string;
}

export interface CategoryRequest {
  name: string;
  description: string;
}
