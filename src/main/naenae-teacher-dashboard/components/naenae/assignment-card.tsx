import { ClipboardList, Eye, CheckCircle2, Clock } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { StatusBadge } from './status-badge'
import { studentAssignment } from '@/lib/naenae-data'

export function AssignmentCard() {
  return (
    <DashboardCard title="오늘의 과제" icon={<ClipboardList className="size-4" />}>
      <div className="flex flex-col gap-3">
        <div className="rounded-2xl bg-muted/60 p-4">
          <div className="mb-2 flex items-start justify-between gap-2">
            <p className="text-base font-bold text-card-foreground text-pretty">
              {studentAssignment.title}
            </p>
            <StatusBadge tone={studentAssignment.tone}>{studentAssignment.status}</StatusBadge>
          </div>
          <p className="flex items-center gap-1 text-sm text-muted-foreground">
            <Clock className="size-4" /> 마감: {studentAssignment.due}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button size="lg" className="rounded-full">
            <Eye /> 과제 확인
          </Button>
          <Button size="lg" variant="outline" className="rounded-full">
            <CheckCircle2 /> 완료 표시
          </Button>
        </div>
      </div>
    </DashboardCard>
  )
}
