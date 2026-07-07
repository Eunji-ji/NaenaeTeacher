import Link from 'next/link'

export function QuickActionButton({
  label,
  icon,
  href = '#',
}: {
  label: string
  icon: React.ReactNode
  href?: string
}) {
  return (
    <Link
      href={href}
      className="group flex items-center gap-2 rounded-full border border-border bg-card px-4 py-2.5 text-sm font-semibold text-card-foreground shadow-sm transition-colors hover:border-primary hover:bg-primary/10 hover:text-primary"
    >
      <span className="flex size-6 items-center justify-center rounded-full bg-primary/12 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
        {icon}
      </span>
      {label}
    </Link>
  )
}
