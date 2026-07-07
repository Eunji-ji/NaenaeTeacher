// ---------------------------------------------------------------------------
// NaenaeTeacher · sample data
// This is static demo data. Replace each field with server data later
// (e.g. Spring Boot model attributes bound in Thymeleaf).
// ---------------------------------------------------------------------------

export type StatusTone = 'success' | 'warning' | 'danger' | 'info' | 'neutral'

// --- Shared: 오늘의 영어 -----------------------------------------------------

export const todayWord = {
  word: 'brave',
  pronunciation: '/breɪv/',
  meaning: '용감한',
  example: 'She is brave enough to try again.',
  exampleMeaning: '그녀는 다시 도전할 만큼 용감하다.',
}

export const todaySentence = {
  sentence: 'I can do better tomorrow.',
  meaning: '나는 내일 더 잘할 수 있어.',
}

// --- Teacher ----------------------------------------------------------------

export const teacher = {
  name: '은지 선생님',
  role: 'Teacher',
  totalStudents: 24,
}

export const teacherSummary = [
  { key: 'present', label: '오늘 출석 완료', value: '18명', icon: 'check', tone: 'success' as StatusTone },
  { key: 'absent', label: '결석 · 지각', value: '2명', icon: 'alert', tone: 'warning' as StatusTone },
  { key: 'assignments', label: '진행 중 과제', value: '5개', icon: 'book', tone: 'info' as StatusTone },
  { key: 'alerts', label: '새 알림', value: '3개', icon: 'bell', tone: 'neutral' as StatusTone },
]

export const teacherAttendance = {
  rate: 90,
  present: 18,
  late: 1,
  absent: 1,
}

export const teacherSchedule = [
  { time: '16:00', title: '초등 영어 A반', students: 8, status: '예정', tone: 'info' as StatusTone },
  { time: '18:00', title: '중등 문법반', students: 10, status: '예정', tone: 'info' as StatusTone },
  { time: '20:00', title: '고등 독해반', students: 6, status: '휴강', tone: 'neutral' as StatusTone },
]

export const teacherStudentStatus = [
  { name: '민준', note: '과제 미완료 · Unit 3', status: '주의', tone: 'warning' as StatusTone },
  { name: '서연', note: '최근 결석 2회', status: '결석', tone: 'danger' as StatusTone },
  { name: '지우', note: '단어 테스트 우수', status: '우수', tone: 'success' as StatusTone },
  { name: '하윤', note: '문장 복습 완료', status: '양호', tone: 'info' as StatusTone },
]

export const teacherNotice = {
  title: '오늘의 알림장',
  summary:
    '오늘 숙제는 Unit 3 단어 10개 외우기입니다. 내일은 짧은 단어 테스트가 있어요.',
  writtenAt: '오늘 15:20',
}

// --- Board (shared shape) ---------------------------------------------------

export const boardPosts = [
  { title: '7월 수업 일정 안내', author: '은지 선생님', time: '2시간 전', comments: 4, badge: '공지', tone: 'info' as StatusTone },
  { title: 'Unit 3 단어장 PDF', author: '은지 선생님', time: '어제', comments: 2, badge: '자료', tone: 'success' as StatusTone },
  { title: '숙제 제출 방법이 궁금해요', author: '민준', time: '어제', comments: 6, badge: '질문', tone: 'warning' as StatusTone },
]

export const teacherQuickActions = [
  { label: '학생 등록', icon: 'userPlus', href: '/teacher/students' },
  { label: '과제 등록', icon: 'clipboard', href: '/teacher/assignments' },
  { label: '알림 작성', icon: 'megaphone', href: '/teacher/notice' },
  { label: '출석 체크', icon: 'check', href: '/teacher/attendance' },
  { label: '오늘의 영어 등록', icon: 'sparkles', href: '/teacher/courses' },
]

// --- Student ----------------------------------------------------------------

export const student = {
  name: '민준',
  role: 'Student',
}

export const studentAttendance = {
  todayStatus: '출석 완료',
  monthRate: 95,
  streak: 5,
  // last 7 days: true = 출석
  week: [true, true, true, false, true, true, true],
}

export const studentAssignment = {
  title: 'Unit 3 단어 10개 외우기',
  due: '오늘 22:00',
  status: '진행 중',
  tone: 'warning' as StatusTone,
}

export const studentProgress = [
  { label: '단어 학습률', value: 82, tone: 'primary' as const },
  { label: '문장 복습률', value: 68, tone: 'lavender' as const },
  { label: '과제 완료율', value: 74, tone: 'warning' as const },
  { label: '출석률', value: 95, tone: 'success' as const },
]

export const studentNotice = {
  title: '오늘의 알림장',
  items: [
    '내일은 Unit 3 단어 테스트가 있어요.',
    '단어장 12쪽을 꼭 복습해 오세요.',
    '준비물: 필기구, 단어 노트',
  ],
}

export const teacherMessage = {
  from: '은지 선생님',
  message:
    '민준아, 이번 주 단어 테스트 정말 좋아졌어! 조금만 더 반복하면 훨씬 안정적일 거야 😊',
}
