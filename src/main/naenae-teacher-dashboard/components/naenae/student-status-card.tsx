import { UserCog } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { StatusBadge } from './status-badge'
import { teacherStudentStatus } from '@/lib/naenae-data'

export function StudentStatusCard() {
  return (
    <DashboardCard title="학생 학습 상태" icon={<UserCog className="size-4" />}>
      <ul className="flex flex-col gap-2">
        {teacherStudentStatus.map((s) => (
          <li key={s.name} className="flex items-center gap-3 rounded-2xl bg-muted/50 p-3">
            <span className="flex size-9 items-center justify-center rounded-full bg-primary/15 text-sm font-bold text-primary">
              {s.name.charAt(0)}
            </span>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-card-foreground">{s.name}</p>
              <p className="truncate text-xs text-muted-foreground">{s.note}</p>
            </div>
            <StatusBadge tone={s.tone}>{s.status}</StatusBadge>
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
