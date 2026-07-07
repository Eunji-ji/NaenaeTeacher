import Link from 'next/link'
import { GraduationCap, BookOpenCheck, ArrowRight } from 'lucide-react'
import { SmileCharacter } from '@/components/naenae/smile-character'

export default function HomePage() {
  return (
    <main className="flex min-h-dvh flex-col items-center justify-center bg-background px-4 py-12">
      <div className="w-full max-w-3xl">
        <div className="mb-8 flex flex-col items-center text-center">
          <SmileCharacter size={72} />
          <h1 className="mt-4 text-3xl font-extrabold text-foreground sm:text-4xl">
            NaenaeTeacher
          </h1>
          <p className="mt-2 max-w-md text-pretty text-muted-foreground">
            학원 선생님과 학생이 함께 쓰는 따뜻한 학습 대시보드. 사용할 화면을 선택해 주세요 🌱
          </p>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <RoleCard
            href="/teacher/dashboard"
            icon={<GraduationCap className="size-6" />}
            title="선생님용"
            subtitle="Teacher Dashboard"
            description="출석 현황, 오늘의 영어, 알림장, 학생 학습 상태를 한눈에 관리해요."
          />
          <RoleCard
            href="/student/dashboard"
            icon={<BookOpenCheck className="size-6" />}
            title="학생용"
            subtitle="Student Dashboard"
            description="오늘의 단어와 문장, 과제, 출석, 선생님의 응원 메시지를 확인해요."
          />
        </div>
      </div>
    </main>
  )
}

function RoleCard({
  href,
  icon,
  title,
  subtitle,
  description,
}: {
  href: string
  icon: React.ReactNode
  title: string
  subtitle: string
  description: string
}) {
  return (
    <Link
      href={href}
      className="group flex flex-col rounded-3xl border border-border bg-card p-6 shadow-sm transition-all hover:-translate-y-1 hover:border-primary hover:shadow-md"
    >
      <span className="flex size-12 items-center justify-center rounded-2xl bg-primary/12 text-primary">
        {icon}
      </span>
      <h2 className="mt-4 text-lg font-extrabold text-card-foreground">{title}</h2>
      <p className="text-xs font-semibold text-muted-foreground">{subtitle}</p>
      <p className="mt-2 text-sm leading-relaxed text-muted-foreground text-pretty">
        {description}
      </p>
      <span className="mt-4 flex items-center gap-1 text-sm font-semibold text-primary">
        들어가기 <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" />
      </span>
    </Link>
  )
}
