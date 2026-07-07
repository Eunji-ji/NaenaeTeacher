import { CheckCircle2, AlertTriangle, BookOpen, Bell } from 'lucide-react'
import { AppShell } from '@/components/naenae/app-shell'
import { WelcomeCard } from '@/components/naenae/welcome-card'
import { SummaryCard } from '@/components/naenae/summary-card'
import { TodayWordCard } from '@/components/naenae/today-word-card'
import { TodaySentenceCard } from '@/components/naenae/today-sentence-card'
import { NoticeCard } from '@/components/naenae/notice-card'
import { BoardListCard } from '@/components/naenae/board-list-card'
import { AttendanceCard } from '@/components/naenae/attendance-card'
import { ScheduleCard } from '@/components/naenae/schedule-card'
import { StudentStatusCard } from '@/components/naenae/student-status-card'
import { QuickActions } from '@/components/naenae/quick-actions'
import { teacher, teacherSummary } from '@/lib/naenae-data'
import { teacherNav } from '../nav'

const summaryIcons = {
  check: <CheckCircle2 className="size-5" />,
  alert: <AlertTriangle className="size-5" />,
  book: <BookOpen className="size-5" />,
  bell: <Bell className="size-5" />,
} as const

export default function TeacherDashboardPage() {
  return (
    <AppShell
      role="Teacher"
      userName={teacher.name}
      pageTitle="Teacher Dashboard"
      navItems={teacherNav}
    >
      <div className="flex flex-col gap-5">
        <WelcomeCard
          greeting={`안녕하세요, ${teacher.name} 😊`}
          message="오늘도 학생들의 작은 성장을 기록해볼까요? 전체 학생 24명과 함께하고 있어요."
        />

        {/* Summary cards */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {teacherSummary.map((item) => (
            <SummaryCard
              key={item.key}
              label={item.label}
              value={item.value}
              tone={item.tone}
              icon={summaryIcons[item.icon as keyof typeof summaryIcons]}
            />
          ))}
        </div>

        {/* Main grid */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          <TodayWordCard variant="teacher" />
          <TodaySentenceCard variant="teacher" />
          <AttendanceCard variant="teacher" />
          <NoticeCard variant="teacher" />
          <ScheduleCard />
          <StudentStatusCard />
          <BoardListCard href="/teacher/board" />
          <div className="md:col-span-2 xl:col-span-2">
            <QuickActions />
          </div>
        </div>
      </div>
    </AppShell>
  )
}
