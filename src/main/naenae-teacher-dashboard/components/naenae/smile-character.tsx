import { cn } from '@/lib/utils'

// A tiny, simple smile mascot used as a friendly brand accent.
export function SmileCharacter({
  className,
  size = 40,
}: {
  className?: string
  size?: number
}) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      role="img"
      aria-label="냬냬 마스코트"
      className={cn('shrink-0', className)}
    >
      <circle cx="24" cy="24" r="22" fill="var(--primary)" opacity="0.18" />
      <circle cx="24" cy="24" r="16" fill="var(--primary)" />
      <circle cx="18.5" cy="21" r="2.2" fill="var(--primary-foreground)" />
      <circle cx="29.5" cy="21" r="2.2" fill="var(--primary-foreground)" />
      <path
        d="M17.5 27.5c1.7 2.6 4 3.9 6.5 3.9s4.8-1.3 6.5-3.9"
        stroke="var(--primary-foreground)"
        strokeWidth="2.4"
        strokeLinecap="round"
      />
      <circle cx="15" cy="26" r="1.8" fill="var(--accent)" opacity="0.8" />
      <circle cx="33" cy="26" r="1.8" fill="var(--accent)" opacity="0.8" />
    </svg>
  )
}
