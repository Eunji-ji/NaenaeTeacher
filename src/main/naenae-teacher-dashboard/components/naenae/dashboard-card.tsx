import { cn } from '@/lib/utils'

export function DashboardCard({
  title,
  icon,
  action,
  emoji,
  className,
  bodyClassName,
  children,
}: {
  title?: string
  icon?: React.ReactNode
  action?: React.ReactNode
  emoji?: string
  className?: string
  bodyClassName?: string
  children: React.ReactNode
}) {
  return (
    <section
      className={cn(
        'flex flex-col rounded-3xl border border-border bg-card p-5 shadow-sm sm:p-6',
        className,
      )}
    >
      {(title || action) && (
        <header className="mb-4 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            {icon && (
              <span className="flex size-8 items-center justify-center rounded-xl bg-primary/10 text-primary">
                {icon}
              </span>
            )}
            {title && (
              <h2 className="text-base font-bold text-card-foreground">
                {title}
                {emoji && <span className="ml-1">{emoji}</span>}
              </h2>
            )}
          </div>
          {action && <div className="shrink-0">{action}</div>}
        </header>
      )}
      <div className={cn('flex-1', bodyClassName)}>{children}</div>
    </section>
  )
}
