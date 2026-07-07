import { TrendingUp } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { ProgressBar } from './progress-bar'
import { studentProgress } from '@/lib/naenae-data'

export function LearningProgressCard() {
  return (
    <DashboardCard title="나의 학습 상태" icon={<TrendingUp className="size-4" />}>
      <ul className="flex flex-col gap-3.5">
        {studentProgress.map((item) => (
          <li key={item.label}>
            <div className="mb-1.5 flex items-center justify-between text-sm">
              <span className="font-medium text-card-foreground">{item.label}</span>
              <span className="font-bold text-muted-foreground">{item.value}%</span>
            </div>
            <ProgressBar value={item.value} tone={item.tone} />
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
