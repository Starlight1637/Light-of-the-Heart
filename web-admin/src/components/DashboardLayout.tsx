import { ReactNode, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { AlertTriangle, FileText, LogOut, Menu, MessageSquare, Users, X } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

interface Props {
  children: ReactNode
}

const menuItems = [
  { path: '/watchlist', icon: AlertTriangle, label: '关注对象', color: 'text-danger' },
  { path: '/reports', icon: FileText, label: '学生报告', color: 'text-secondary' },
  { path: '/batch', icon: Users, label: '批量建号', color: 'text-warning' },
  { path: '/feedback', icon: MessageSquare, label: '用户反馈', color: 'text-tertiary' },
]

export default function DashboardLayout({ children }: Props) {
  const location = useLocation()
  const navigate = useNavigate()
  const { account, school, logout } = useAuthStore()
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const handleLogout = () => {
    if (confirm('确定退出登录吗？')) {
      logout()
      navigate('/login')
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="fixed inset-x-0 top-0 z-10 bg-white shadow-sm">
        <div className="flex items-center justify-between px-6 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="rounded-lg p-2 hover:bg-gray-100 lg:hidden"
              aria-label="切换侧边栏"
            >
              {sidebarOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
            <h1 className="text-2xl font-bold text-primary">心光管理系统</h1>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">{account}</p>
              <p className="text-xs text-gray-500">{school} · 管理员</p>
            </div>
            <button
              onClick={handleLogout}
              className="rounded-lg p-2 text-danger transition-colors hover:bg-red-50"
              title="退出登录"
            >
              <LogOut size={20} />
            </button>
          </div>
        </div>
      </header>

      <div className="flex pt-16">
        <aside
          className={`fixed bottom-0 left-0 top-16 z-20 w-64 bg-white shadow-sm transition-transform duration-300 ${
            sidebarOpen ? 'translate-x-0' : '-translate-x-full'
          } lg:translate-x-0`}
        >
          <nav className="space-y-2 p-4">
            {menuItems.map((item) => {
              const Icon = item.icon
              const isActive = location.pathname === item.path
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-3 rounded-lg px-4 py-3 transition-colors ${
                    isActive
                      ? 'bg-primary/10 font-medium text-primary'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <Icon size={20} className={isActive ? item.color : ''} />
                  <span>{item.label}</span>
                </Link>
              )
            })}
          </nav>
        </aside>

        <main className={`flex-1 p-6 transition-all duration-300 ${sidebarOpen ? 'lg:ml-64' : ''}`}>
          <div className="mx-auto max-w-7xl">{children}</div>
        </main>
      </div>
    </div>
  )
}
