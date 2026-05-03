import { useState } from 'react'
import { CheckCircle, Download, Users } from 'lucide-react'
import { adminApi } from '../api/admin'

interface CreatedAccount {
  account: string
  password: string
}

export default function BatchAccountPage() {
  const [formData, setFormData] = useState({
    school: '心光大学',
    accountsText: '',
    defaultPassword: 'mindful123',
    role: 'user' as 'user' | 'admin',
  })
  const [loading, setLoading] = useState(false)
  const [accounts, setAccounts] = useState<CreatedAccount[]>([])
  const [message, setMessage] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      const accountsList = formData.accountsText
        .split(/\r?\n|,/)
        .map((item) => item.trim())
        .filter(Boolean)
      const result = await adminApi.batchCreateAccounts(
        formData.school,
        accountsList,
        formData.defaultPassword,
        formData.role
      )
      setAccounts(result.accounts)
      setMessage(result.message)
    } catch (error: any) {
      alert(error.response?.data?.detail || '创建失败')
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = () => {
    const csv = [
      '账号,密码,学校',
      ...accounts.map((acc) => `${acc.account},${acc.password},${formData.school}`),
    ].join('\n')

    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `accounts_${Date.now()}.csv`
    link.click()
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="mb-2 text-2xl font-bold text-gray-900">批量建号</h2>
        <p className="text-gray-600">按用户端登录规则创建学校、账号和初始密码</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="card">
          <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
            <Users className="text-primary" size={20} />
            创建设置
          </h3>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">学校名称</label>
              <input
                type="text"
                className="input"
                value={formData.school}
                onChange={(e) => setFormData({ ...formData, school: e.target.value })}
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">账号列表</label>
              <textarea
                className="input min-h-40"
                value={formData.accountsText}
                onChange={(e) => setFormData({ ...formData, accountsText: e.target.value })}
                placeholder="每行一个账号，也可以用英文逗号分隔"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">默认密码</label>
              <input
                type="text"
                className="input"
                value={formData.defaultPassword}
                onChange={(e) => setFormData({ ...formData, defaultPassword: e.target.value })}
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">账号角色</label>
              <select
                className="input"
                value={formData.role}
                onChange={(e) => setFormData({ ...formData, role: e.target.value as 'user' | 'admin' })}
              >
                <option value="user">用户</option>
                <option value="admin">管理员</option>
              </select>
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full disabled:opacity-50">
              {loading ? '创建中...' : '开始创建'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 className="mb-4 text-lg font-semibold">创建结果</h3>

          {message ? (
            <div>
              <div className="mb-4 flex items-center gap-2 rounded-lg bg-green-50 px-4 py-3 text-green-700">
                <CheckCircle size={20} />
                <span>{message}</span>
              </div>

              {accounts.length > 0 && (
                <button onClick={handleDownload} className="btn-secondary mb-4 flex w-full items-center justify-center gap-2">
                  <Download size={18} />
                  下载账号列表 CSV
                </button>
              )}

              <div className="max-h-96 overflow-y-auto rounded-lg border border-gray-200">
                <table className="w-full text-sm">
                  <thead className="sticky top-0 bg-gray-50">
                    <tr>
                      <th className="px-4 py-2 text-left">账号</th>
                      <th className="px-4 py-2 text-left">密码</th>
                    </tr>
                  </thead>
                  <tbody>
                    {accounts.map((acc) => (
                      <tr key={acc.account} className="border-t">
                        <td className="px-4 py-2 font-mono">{acc.account}</td>
                        <td className="px-4 py-2 font-mono">{acc.password}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center text-gray-500">
              <Users className="mx-auto mb-4" size={48} />
              <p>填写左侧表单后开始创建账号</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
