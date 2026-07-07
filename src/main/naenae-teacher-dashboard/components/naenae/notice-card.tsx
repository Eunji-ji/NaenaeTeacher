import { Megaphone, Pencil, ArrowRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { teacherNotice, studentNotice } from '@/lib/naenae-data'

export function NoticeCard({ variant }: { variant: 'teacher' | 'student' }) {
  if (variant === 'teacher') {
    return (
      <DashboardCard
        title="알림장"
        icon={<Megaphone className="size-4" />}
        action={
          <Button size="sm" variant="ghost" className="rounded-full text-primary">
            전체 보기 <ArrowRight />
          </Button>
        }
      >
        <div className="flex h-full flex-col gap-3">
          <div className="rounded-2xl bg-muted/60 p-4">
            <p className="text-sm font-semibold text-card-foreground">{teacherNotice.title}</p>
            <p className="mt-1 text-sm leading-relaxed text-muted-foreground">
              {teacherNotice.summary}
            </p>
            <p className="mt-2 text-xs text-muted-foreground">{teacherNotice.writtenAt}</p>
          </div>
          <Button className="mt-auto rounded-full" size="lg">
            <Pencil /> 알림 작성
          </Button>
        </div>
      </DashboardCard>
    )
  }

  return (
    <DashboardCard title="알림장" icon={<Megaphone className="size-4" />}>
      <ul className="flex flex-col gap-2">
        {studentNotice.items.map((item) => (
          <li
            key={item}
            className="flex items-start gap-2 rounded-2xl bg-muted/50 p-3 text-sm text-card-foreground"
          >
            <span className="mt-0.5 size-1.5 shrink-0 rounded-full bg-primary" />
            <span className="leading-relaxed">{item}</span>
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
