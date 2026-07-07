import { cn } from '@/lib/utils'
import type { StatusTone } from '@/lib/naenae-data'

const toneStyles: Record<StatusTone, string> = {
  success: 'bg-success/15 text-success-foreground',
  warning: 'bg-warning/20 text-warning-foreground',
  danger: 'bg-destructive/15 text-destructive',
  info: 'bg-secondary text-secondary-foreground',
  neutral: 'bg-lavender/25 text-lavender-foreground',
}

export function SummaryCard({
  label,
  value,
  icon,
  tone = 'neutral',
}: {
  label: string
  value: string
  icon: React.ReactNode
  tone?: StatusTone
}) {
  return (
    <div className="flex items-center gap-4 rounded-3xl border border-border bg-card p-4 shadow-sm sm:p-5">
      <span
        className={cn(
          'flex size-11 shrink-0 items-center justify-center rounded-2xl',
          toneStyles[tone],
        )}
      >
        {icon}
      </span>
      <div className="min-w-0">
        <p className="truncate text-sm text-muted-foreground">{label}</p>
        <p className="text-xl font-bold text-card-foreground">{value}</p>
      </div>
    </div>
  )
}
