import type { NavItem } from '@/components/naenae/app-shell'

export const teacherNav: NavItem[] = [
  { label: 'Dashboard', href: '/teacher/dashboard', icon: 'dashboard' },
  { label: '학생 관리', href: '/teacher/students', icon: 'users' },
  { label: '수업 관리', href: '/teacher/courses', icon: 'book' },
  { label: '과제 관리', href: '/teacher/assignments', icon: 'clipboard' },
  { label: '출석 관리', href: '/teacher/attendance', icon: 'attendance' },
  { label: '오늘의 영어', href: '/teacher/today-english', icon: 'sparkles' },
  { label: '알림장', href: '/teacher/notice', icon: 'megaphone' },
  { label: '게시판', href: '/teacher/board', icon: 'board' },
  { label: '설정', href: '/teacher/settings', icon: 'settings' },
]
