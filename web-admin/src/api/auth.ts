import apiClient from './client'

export interface LoginRequest {
  school: string
  account: string
  password: string
}

export interface LoginResponse {
  token: string
  user_id: number
  school: string
  account: string
  role: string
}

export const authApi = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/login', data)
    return response.data
  },

  changePassword: async (oldPassword: string, newPassword: string) => {
    const response = await apiClient.post('/auth/change-password', {
      old_password: oldPassword,
      new_password: newPassword,
    })
    return response.data
  },
}
