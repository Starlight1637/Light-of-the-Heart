import { useEffect, useState } from 'react'
import { Calendar, CheckCircle, Clock, MessageSquare, User } from 'lucide-react'
import { adminApi, Feedback } from '../api/admin'

export default function FeedbackPage() {
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      setFeedbacks(await adminApi.getFeedback())
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateStatus = async (feedbackId: string) => {
    await adminApi.updateFeedbackStatus(feedbackId, 'resolved')
    await loadData()
  }

  if (loading) {
    return <div className="flex h-64 items-center justify-center text-gray-500">加载中...</div>
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="mb-2 text-2xl font-bold text-gray-900">用户反馈</h2>
        <p className="text-gray-600">用户端提交的意见和问题</p>
      </div>

      {feedbacks.length === 0 ? (
        <div className="card py-12 text-center">
          <MessageSquare className="mx-auto mb-4 text-gray-400" size={48} />
          <p className="text-gray-500">暂无反馈</p>
        </div>
      ) : (
        <div className="space-y-4">
          {feedbacks.map((feedback) => (
            <div key={feedback.id} className="card transition-shadow hover:shadow-md">
              <div className="mb-4 flex items-start justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-tertiary/10">
                    <User className="text-tertiary" size={20} />
                  </div>
                  <div>
                    <h3 className="font-medium text-gray-900">{feedback.account}</h3>
                    <p className="flex items-center gap-1 text-xs text-gray-500">
                      <Calendar size={12} />
                      {new Date(feedback.createdAt).toLocaleString()}
                    </p>
                  </div>
                </div>
                <span
                  className={`flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium ${
                    feedback.status === 'resolved'
                      ? 'bg-green-100 text-green-700'
                      : 'bg-orange-100 text-orange-700'
                  }`}
                >
                  {feedback.status === 'resolved' ? <CheckCircle size={14} /> : <Clock size={14} />}
                  {feedback.status === 'resolved' ? '已处理' : '待处理'}
                </span>
              </div>

              <p className="mb-4 whitespace-pre-wrap text-gray-700">{feedback.content}</p>

              {feedback.status !== 'resolved' && (
                <button onClick={() => handleUpdateStatus(feedback.id)} className="btn-primary text-sm">
                  标记为已处理
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
