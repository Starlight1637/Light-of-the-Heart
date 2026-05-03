import { useEffect, useState } from 'react'
import { Calendar, CheckCircle, FileText, User } from 'lucide-react'
import { adminApi, AdminReport } from '../api/admin'

export default function ReportsPage() {
  const [reports, setReports] = useState<AdminReport[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      setReports(await adminApi.getReports())
    } finally {
      setLoading(false)
    }
  }

  const markReviewed = async (reportId: string) => {
    await adminApi.markReportReviewed(reportId)
    await loadData()
  }

  if (loading) {
    return <div className="flex h-64 items-center justify-center text-gray-500">加载中...</div>
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="mb-2 text-2xl font-bold text-gray-900">学生报告</h2>
        <p className="text-gray-600">学生从用户端提交给管理员的内容</p>
      </div>

      {reports.length === 0 ? (
        <div className="card py-12 text-center">
          <FileText className="mx-auto mb-4 text-gray-400" size={48} />
          <p className="text-gray-500">暂无报告</p>
        </div>
      ) : (
        <div className="space-y-4">
          {reports.map((report) => (
            <div key={report.id} className="card transition-shadow hover:shadow-md">
              <div className="mb-4 flex items-start justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-secondary/10">
                    <User className="text-secondary" size={20} />
                  </div>
                  <div>
                    <h3 className="font-medium text-gray-900">{report.account}</h3>
                    <p className="flex items-center gap-1 text-xs text-gray-500">
                      <Calendar size={12} />
                      {new Date(report.reportDate).toLocaleString()}
                    </p>
                  </div>
                </div>
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${
                    report.isReviewed ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'
                  }`}
                >
                  {report.isReviewed ? '已审核' : '待审核'}
                </span>
              </div>

              <p className="mb-4 whitespace-pre-wrap text-gray-700">{report.content}</p>

              {!report.isReviewed && (
                <button onClick={() => markReviewed(report.id)} className="btn-primary flex items-center gap-2 text-sm">
                  <CheckCircle size={16} />
                  标记为已审核
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
