import { Heart } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { teacherMessage } from '@/lib/naenae-data'

export function TeacherMessageCard() {
  return (
    <DashboardCard
      title="선생님 응원 메시지"
      icon={<Heart className="size-4" />}
      className="bg-gradient-to-br from-lavender/20 to-card"
    >
      <div className="relative rounded-2xl bg-card p-4 shadow-sm">
        <span className="absolute -top-2 left-4 text-2xl leading-none" aria-hidden>
          💬
        </span>
        <p className="text-sm leading-relaxed text-card-foreground text-pretty">
          {teacherMessage.message}
        </p>
        <p className="mt-2 text-right text-xs font-semibold text-muted-foreground">
          - {teacherMessage.from}
        </p>
      </div>
    </DashboardCard>
  )
}
