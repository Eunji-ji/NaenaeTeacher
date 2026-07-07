'use client'

import { useState } from 'react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  LayoutDashboard,
  Users,
  BookOpen,
  ClipboardList,
  CalendarCheck,
  Sparkles,
  Megaphone,
  MessageSquare,
  Settings,
  User,
  Bell,
  Menu,
  X,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { SmileCharacter } from './smile-character'

const iconMap = {
  dashboard: LayoutDashboard,
  users: Users,
  book: BookOpen,
  clipboard: ClipboardList,
  attendance: CalendarCheck,
  sparkles: Sparkles,
  megaphone: Megaphone,
  board: MessageSquare,
  settings: Settings,
  user: User,
} as const

export type NavItem = {
  label: string
  href: string
  icon: keyof typeof iconMap
}

export function AppShell({
  role,
  userName,
  pageTitle,
  navItems,
  children,
}: {
  role: 'Teacher' | 'Student'
  userName: string
  pageTitle: string
  navItems: NavItem[]
  children: React.ReactNode
}) {
  const pathname = usePathname()
  const [mobileOpen, setMobileOpen] = useState(false)

  // Only the primary items appear in the mobile bottom bar.
  const bottomNav = navItems.slice(0, 5)

  const NavLink = ({ item, onClick }: { item: NavItem; onClick?: () => void }) => {
    const Icon = iconMap[item.icon]
    const active = pathname === item.href
    return (
      <Link
        href={item.href}
        onClick={onClick}
        aria-current={active ? 'page' : undefined}
        className={cn(
          'flex items-center gap-3 rounded-2xl px-3.5 py-2.5 text-sm font-semibold transition-colors',
          active
            ? 'bg-sidebar-accent text-sidebar-accent-foreground'
            : 'text-muted-foreground hover:bg-muted hover:text-foreground',
        )}
      >
        <Icon className="size-5 shrink-0" />
        {item.label}
      </Link>
    )
  }

  return (
    <div className="min-h-dvh bg-background">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-sidebar-border bg-sidebar lg:flex">
        <div className="flex items-center gap-2 px-6 py-5">
          <SmileCharacter size={36} />
          <div className="leading-tight">
            <p className="text-base font-extrabold text-sidebar-foreground">NaenaeTeacher</p>
            <p className="text-xs text-muted-foreground">{role} Space</p>
          </div>
        </div>
        <nav className="flex flex-1 flex-col gap-1 overflow-y-auto px-3 py-2">
          {navItems.map((item) => (
            <NavLink key={item.href} item={item} />
          ))}
        </nav>
      </aside>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-foreground/40"
            onClick={() => setMobileOpen(false)}
            aria-hidden
          />
          <div className="absolute inset-y-0 left-0 flex w-72 max-w-[80%] flex-col bg-sidebar p-4 shadow-xl">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <SmileCharacter size={32} />
                <span className="font-extrabold text-sidebar-foreground">NaenaeTeacher</span>
              </div>
              <button
                onClick={() => setMobileOpen(false)}
                aria-label="메뉴 닫기"
                className="flex size-9 items-center justify-center rounded-full text-muted-foreground hover:bg-muted"
              >
                <X className="size-5" />
              </button>
            </div>
            <nav className="flex flex-col gap-1">
              {navItems.map((item) => (
                <NavLink key={item.href} item={item} onClick={() => setMobileOpen(false)} />
              ))}
            </nav>
          </div>
        </div>
      )}

      {/* Main column */}
      <div className="lg:pl-64">
        {/* Header */}
        <header className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-border bg-background/85 px-4 py-3 backdrop-blur sm:px-6">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setMobileOpen(true)}
              aria-label="메뉴 열기"
              className="flex size-9 items-center justify-center rounded-full text-muted-foreground hover:bg-muted lg:hidden"
            >
              <Menu className="size-5" />
            </button>
            <div className="leading-tight">
              <p className="text-xs font-medium text-muted-foreground">{pageTitle}</p>
              <h1 className="text-lg font-extrabold text-foreground">
                {role === 'Teacher' ? '선생님 대시보드' : '학생 대시보드'}
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              aria-label="알림"
              className="relative flex size-10 items-center justify-center rounded-full border border-border bg-card text-muted-foreground transition-colors hover:text-foreground"
            >
              <Bell className="size-5" />
              <span className="absolute right-2.5 top-2.5 size-2 rounded-full bg-destructive" />
            </button>
            <div className="flex items-center gap-2 rounded-full border border-border bg-card py-1 pl-1 pr-3">
              <span className="flex size-8 items-center justify-center rounded-full bg-primary/15 text-sm font-bold text-primary">
                {userName.charAt(0)}
              </span>
              <span className="hidden text-sm font-semibold text-foreground sm:inline">
                {userName} 😊
              </span>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="mx-auto max-w-7xl px-4 pb-28 pt-5 sm:px-6 lg:pb-10">{children}</main>
      </div>

      {/* Mobile bottom navigation */}
      <nav className="fixed inset-x-0 bottom-0 z-30 flex items-center justify-around border-t border-border bg-card/95 px-2 py-2 backdrop-blur lg:hidden">
        {bottomNav.map((item) => {
          const Icon = iconMap[item.icon]
          const active = pathname === item.href
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? 'page' : undefined}
              className={cn(
                'flex flex-1 flex-col items-center gap-1 rounded-2xl py-1.5 text-[11px] font-semibold transition-colors',
                active ? 'text-primary' : 'text-muted-foreground',
              )}
            >
              <Icon className="size-5" />
              {item.label}
            </Link>
          )
        })}
      </nav>
    </div>
  )
}
