import type { NavItem } from '@/components/naenae/app-shell'

export const studentNav: NavItem[] = [
  { label: 'Dashboard', href: '/student/dashboard', icon: 'dashboard' },
  { label: '오늘의 영어', href: '/student/word', icon: 'sparkles' },
  { label: '과제', href: '/student/assignments', icon: 'clipboard' },
  { label: '출석', href: '/student/attendance', icon: 'attendance' },
  { label: '알림장', href: '/student/notice', icon: 'megaphone' },
  { label: '게시판', href: '/student/board', icon: 'board' },
  { label: '내 정보', href: '/student/profile', icon: 'user' },
]
