import { AppShell } from '@/components/naenae/app-shell'
import { WelcomeCard } from '@/components/naenae/welcome-card'
import { TodayWordCard } from '@/components/naenae/today-word-card'
import { TodaySentenceCard } from '@/components/naenae/today-sentence-card'
import { AttendanceCard } from '@/components/naenae/attendance-card'
import { AssignmentCard } from '@/components/naenae/assignment-card'
import { NoticeCard } from '@/components/naenae/notice-card'
import { BoardListCard } from '@/components/naenae/board-list-card'
import { LearningProgressCard } from '@/components/naenae/learning-progress-card'
import { TeacherMessageCard } from '@/components/naenae/teacher-message-card'
import { student } from '@/lib/naenae-data'
import { studentNav } from '../nav'

export default function StudentDashboardPage() {
  return (
    <AppShell
      role="Student"
      userName={student.name}
      pageTitle="Student Dashboard"
      navItems={studentNav}
    >
      <div className="flex flex-col gap-5">
        <WelcomeCard
          greeting={`안녕, ${student.name} 😊`}
          message="오늘의 영어 한 문장부터 가볍게 시작해볼까? 이번 주도 잘하고 있어!"
        />

        {/* Priority learning cards first (also first on mobile) */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <TodayWordCard variant="student" />
          <TodaySentenceCard variant="student" />
        </div>

        {/* Secondary info grid */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          <AttendanceCard variant="student" />
          <AssignmentCard />
          <LearningProgressCard />
          <NoticeCard variant="student" />
          <TeacherMessageCard />
          <BoardListCard href="/student/board" />
        </div>
      </div>
    </AppShell>
  )
}
