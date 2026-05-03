import apiClient from './client'

export interface WatchListUser {
  user_id: number
  userId: number
  account: string
  school: string
  latest_risk_level: string
  riskLevel: 'low' | 'medium' | 'high' | 'critical'
  is_handled: boolean
  status: 'active' | 'handled'
  flaggedAt: string
  latestKeywords: string[]
}

export interface FlaggedEntry {
  id: number
  entryId: string
  post_id?: string | null
  risk_level: string
  riskLevel: string
  content_preview: string
  content: string
  riskKeywords: string[]
  flagged_at: string
  timestamp: string
}

export interface AdminReport {
  id: string
  user_id: number
  userId: number
  account: string
  school: string
  post_id?: string | null
  content: string
  moodSummary: string
  riskIndicators: string[]
  is_reviewed: boolean
  isReviewed: boolean
  created_at: string
  reportDate: string
}

export interface Feedback {
  id: string
  user_id: number
  userId: number
  account: string
  school: string
  content: string
  created_at: string
  createdAt: string
  is_read: boolean
  status: 'pending' | 'resolved'
}

export interface BatchAccountResponse {
  created: number
  skipped: number
  totalRequested: number
  successCount: number
  failedCount: number
  skippedAccounts: string[]
  accounts: Array<{ account: string; password: string }>
  message: string
}

export const adminApi = {
  getWatchList: async (): Promise<WatchListUser[]> => {
    const response = await apiClient.get('/admin/watchlist')
    return response.data
  },

  getFlaggedEntries: async (userId: number): Promise<FlaggedEntry[]> => {
    const response = await apiClient.get(`/admin/watchlist/${userId}/entries`)
    return response.data
  },

  markWatchListHandled: async (userId: number) => {
    const response = await apiClient.put(`/admin/watchlist/${userId}/handle`)
    return response.data
  },

  getReports: async (): Promise<AdminReport[]> => {
    const response = await apiClient.get('/admin/reports')
    return response.data
  },

  markReportReviewed: async (reportId: string) => {
    const response = await apiClient.put(`/admin/reports/${reportId}/review`)
    return response.data
  },

  batchCreateAccounts: async (
    school: string,
    accounts: string[],
    defaultPassword: string,
    role: 'user' | 'admin' = 'user'
  ): Promise<BatchAccountResponse> => {
    const response = await apiClient.post('/admin/accounts/batch', {
      school,
      accounts,
      default_password: defaultPassword,
      role,
    })
    return response.data
  },

  getFeedback: async (): Promise<Feedback[]> => {
    const response = await apiClient.get('/admin/feedback')
    return response.data
  },

  updateFeedbackStatus: async (feedbackId: string, status: 'pending' | 'resolved') => {
    const response = await apiClient.patch(`/admin/feedback/${feedbackId}`, { status })
    return response.data
  },
}
