import { UserPlus, ClipboardList, Megaphone, Check, Sparkles } from 'lucide-react'
import { QuickActionButton } from './quick-action-button'
import { teacherQuickActions } from '@/lib/naenae-data'

const iconMap = {
  userPlus: UserPlus,
  clipboard: ClipboardList,
  megaphone: Megaphone,
  check: Check,
  sparkles: Sparkles,
} as const

export function QuickActions() {
  return (
    <section className="rounded-3xl border border-border bg-card p-5 shadow-sm sm:p-6">
      <h2 className="mb-4 text-base font-bold text-card-foreground">빠른 액션 ⚡</h2>
      <div className="flex flex-wrap gap-2.5">
        {teacherQuickActions.map((action) => {
          const Icon = iconMap[action.icon as keyof typeof iconMap]
          return (
            <QuickActionButton
              key={action.label}
              label={action.label}
              href={action.href}
              icon={<Icon className="size-3.5" />}
            />
          )
        })}
      </div>
    </section>
  )
}
