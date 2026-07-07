import { cn } from '@/lib/utils'
import type { StatusTone } from '@/lib/naenae-data'

const toneStyles: Record<StatusTone, string> = {
  success: 'bg-success/20 text-success-foreground',
  warning: 'bg-warning/25 text-warning-foreground',
  danger: 'bg-destructive/15 text-destructive',
  info: 'bg-secondary text-secondary-foreground',
  neutral: 'bg-muted text-muted-foreground',
}

export function StatusBadge({
  children,
  tone = 'neutral',
  className,
}: {
  children: React.ReactNode
  tone?: StatusTone
  className?: string
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold',
        toneStyles[tone],
        className,
      )}
    >
      {children}
    </span>
  )
}
