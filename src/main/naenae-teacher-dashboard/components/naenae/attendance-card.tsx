import { CalendarCheck, Check } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { ProgressBar } from './progress-bar'
import { teacherAttendance, studentAttendance } from '@/lib/naenae-data'

export function AttendanceCard({ variant }: { variant: 'teacher' | 'student' }) {
  if (variant === 'teacher') {
    return (
      <DashboardCard title="출석 현황" icon={<CalendarCheck className="size-4" />}>
        <div className="flex h-full flex-col gap-4">
          <div>
            <div className="mb-1.5 flex items-baseline justify-between">
              <span className="text-sm text-muted-foreground">출석률</span>
              <span className="text-2xl font-extrabold text-primary">
                {teacherAttendance.rate}%
              </span>
            </div>
            <ProgressBar value={teacherAttendance.rate} tone="primary" />
          </div>
          <div className="grid grid-cols-3 gap-2 text-center">
            <Stat label="출석" value={teacherAttendance.present} tone="text-success-foreground" />
            <Stat label="지각" value={teacherAttendance.late} tone="text-warning-foreground" />
            <Stat label="결석" value={teacherAttendance.absent} tone="text-destructive" />
          </div>
          <Button className="mt-auto rounded-full" size="lg">
            <Check /> 출석 체크하기
          </Button>
        </div>
      </DashboardCard>
    )
  }

  const dayLabels = ['월', '화', '수', '목', '금', '토', '일']
  return (
    <DashboardCard title="나의 출석" emoji="😊" icon={<CalendarCheck className="size-4" />}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between rounded-2xl bg-success/15 p-3">
          <div>
            <p className="text-sm text-success-foreground/80">오늘</p>
            <p className="text-lg font-bold text-success-foreground">
              {studentAttendance.todayStatus}
            </p>
          </div>
          <span className="flex size-10 items-center justify-center rounded-full bg-success text-success-foreground">
            <Check className="size-6" />
          </span>
        </div>

        <div className="flex items-center justify-between gap-2">
          {studentAttendance.week.map((present, i) => (
            <div key={i} className="flex flex-1 flex-col items-center gap-1">
              <span className="text-xs text-muted-foreground">{dayLabels[i]}</span>
              <span
                className={
                  present
                    ? 'flex size-8 items-center justify-center rounded-full bg-primary text-primary-foreground'
                    : 'flex size-8 items-center justify-center rounded-full border border-dashed border-border text-muted-foreground'
                }
              >
                {present ? <Check className="size-4" /> : ''}
              </span>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-2 gap-2 text-center">
          <Stat label="이번 달 출석률" value={`${studentAttendance.monthRate}%`} tone="text-primary" />
          <Stat label="연속 출석" value={`${studentAttendance.streak}일`} tone="text-lavender-foreground" />
        </div>
      </div>
    </DashboardCard>
  )
}

function Stat({
  label,
  value,
  tone,
}: {
  label: string
  value: string | number
  tone: string
}) {
  return (
    <div className="rounded-2xl bg-muted/60 p-3">
      <p className={`text-xl font-extrabold ${tone}`}>{value}</p>
      <p className="text-xs text-muted-foreground">{label}</p>
    </div>
  )
}
