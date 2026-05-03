import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import LoginPage from './pages/LoginPage'
import DashboardLayout from './components/DashboardLayout'
import WatchListPage from './pages/WatchListPage'
import ReportsPage from './pages/ReportsPage'
import BatchAccountPage from './pages/BatchAccountPage'
import FeedbackPage from './pages/FeedbackPage'

function App() {
  const { isAuthenticated } = useAuthStore()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/*"
          element={
            isAuthenticated ? (
              <DashboardLayout>
                <Routes>
                  <Route path="/" element={<Navigate to="/watchlist" replace />} />
                  <Route path="/watchlist" element={<WatchListPage />} />
                  <Route path="/reports" element={<ReportsPage />} />
                  <Route path="/batch" element={<BatchAccountPage />} />
                  <Route path="/feedback" element={<FeedbackPage />} />
                </Routes>
              </DashboardLayout>
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App
