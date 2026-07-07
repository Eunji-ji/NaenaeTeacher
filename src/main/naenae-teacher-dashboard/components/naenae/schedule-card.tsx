import { Clock, Users } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { StatusBadge } from './status-badge'
import { teacherSchedule } from '@/lib/naenae-data'

export function ScheduleCard() {
  return (
    <DashboardCard title="오늘 수업 일정" icon={<Clock className="size-4" />}>
      <ul className="flex flex-col gap-2">
        {teacherSchedule.map((item) => (
          <li
            key={item.time + item.title}
            className="flex items-center gap-3 rounded-2xl bg-muted/50 p-3"
          >
            <span className="flex flex-col items-center rounded-xl bg-card px-3 py-1.5 text-sm font-bold text-primary shadow-sm">
              {item.time}
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-card-foreground">{item.title}</p>
              <p className="flex items-center gap-1 text-xs text-muted-foreground">
                <Users className="size-3.5" /> {item.students}명
              </p>
            </div>
            <StatusBadge tone={item.tone}>{item.status}</StatusBadge>
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
