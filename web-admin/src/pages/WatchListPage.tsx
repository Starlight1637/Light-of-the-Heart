import { useEffect, useState } from 'react'
import { AlertTriangle, Calendar, Check, FileText, User } from 'lucide-react'
import { adminApi, FlaggedEntry, WatchListUser } from '../api/admin'

const riskStyle: Record<string, string> = {
  critical: 'bg-red-100 text-red-700 border-red-200',
  high: 'bg-red-100 text-red-700 border-red-200',
  medium: 'bg-orange-100 text-orange-700 border-orange-200',
  low: 'bg-green-100 text-green-700 border-green-200',
}

const riskLabel: Record<string, string> = {
  critical: '危急',
  high: '高风险',
  medium: '中风险',
  low: '低风险',
}

export default function WatchListPage() {
  const [users, setUsers] = useState<WatchListUser[]>([])
  const [selected, setSelected] = useState<WatchListUser | null>(null)
  const [entries, setEntries] = useState<FlaggedEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [detailLoading, setDetailLoading] = useState(false)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      setUsers(await adminApi.getWatchList())
    } finally {
      setLoading(false)
    }
  }

  const openDetail = async (user: WatchListUser) => {
    setSelected(user)
    setDetailLoading(true)
    try {
      setEntries(await adminApi.getFlaggedEntries(user.user_id))
    } finally {
      setDetailLoading(false)
    }
  }

  const markHandled = async (user: WatchListUser) => {
    await adminApi.markWatchListHandled(user.user_id)
    setSelected(null)
    setEntries([])
    await loadData()
  }

  if (loading) {
    return <div className="flex h-64 items-center justify-center text-gray-500">加载中...</div>
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="mb-2 text-2xl font-bold text-gray-900">关注对象</h2>
        <p className="text-gray-600">来自用户端高风险日记和紧急提醒的学生列表</p>
      </div>

      {users.length === 0 ? (
        <div className="card py-12 text-center">
          <AlertTriangle className="mx-auto mb-4 text-gray-400" size={48} />
          <p className="text-gray-500">暂无需要关注的对象</p>
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-[1fr_420px]">
          <div className="grid gap-4 md:grid-cols-2">
            {users.map((user) => (
              <button
                key={user.user_id}
                onClick={() => openDetail(user)}
                className="card text-left transition-shadow hover:shadow-md"
              >
                <div className="mb-4 flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                      <User className="text-primary" size={24} />
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-900">{user.account}</h3>
                      <p className="text-sm text-gray-500">{user.school}</p>
                    </div>
                  </div>
                  <span className={`rounded-full border px-3 py-1 text-xs font-medium ${riskStyle[user.riskLevel]}`}>
                    {riskLabel[user.riskLevel]}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Calendar size={16} />
                  <span>更新时间：{new Date(user.flaggedAt).toLocaleString()}</span>
                </div>
              </button>
            ))}
          </div>

          <div className="card h-fit">
            {selected ? (
              <div>
                <div className="mb-4 flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">{selected.account}</h3>
                    <p className="text-sm text-gray-500">风险记录</p>
                  </div>
                  <button onClick={() => markHandled(selected)} className="btn-primary flex items-center gap-2 text-sm">
                    <Check size={16} />
                    标记处理
                  </button>
                </div>
                {detailLoading ? (
                  <p className="text-gray-500">加载详情中...</p>
                ) : entries.length === 0 ? (
                  <p className="text-gray-500">暂无风险条目</p>
                ) : (
                  <div className="space-y-3">
                    {entries.map((entry) => (
                      <div key={entry.entryId} className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                        <div className="mb-2 flex items-center justify-between">
                          <span className={`rounded-full px-2 py-1 text-xs ${riskStyle[entry.riskLevel]}`}>
                            {riskLabel[entry.riskLevel] || entry.risk_level}
                          </span>
                          <span className="text-xs text-gray-500">{new Date(entry.timestamp).toLocaleString()}</span>
                        </div>
                        <p className="text-sm text-gray-700">{entry.content}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="py-8 text-center text-gray-500">
                <FileText className="mx-auto mb-3" size={40} />
                <p>选择左侧学生查看详情</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
