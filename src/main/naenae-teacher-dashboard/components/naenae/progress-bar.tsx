import { cn } from '@/lib/utils'

type ProgressTone = 'primary' | 'success' | 'warning' | 'lavender'

const toneStyles: Record<ProgressTone, string> = {
  primary: 'bg-primary',
  success: 'bg-success',
  warning: 'bg-warning',
  lavender: 'bg-lavender',
}

export function ProgressBar({
  value,
  tone = 'primary',
  className,
}: {
  value: number
  tone?: ProgressTone
  className?: string
}) {
  const clamped = Math.max(0, Math.min(100, value))
  return (
    <div
      className={cn('h-2.5 w-full overflow-hidden rounded-full bg-muted', className)}
      role="progressbar"
      aria-valuenow={clamped}
      aria-valuemin={0}
      aria-valuemax={100}
    >
      <div
        className={cn('h-full rounded-full transition-all duration-500', toneStyles[tone])}
        style={{ width: `${clamped}%` }}
      />
    </div>
  )
}
