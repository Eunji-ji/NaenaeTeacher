import { Sparkles, Pencil, Eye, CheckCircle2, Volume2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { todayWord } from '@/lib/naenae-data'

export function TodayWordCard({ variant }: { variant: 'teacher' | 'student' }) {
  return (
    <DashboardCard
      title="오늘의 단어"
      emoji="🌱"
      icon={<Sparkles className="size-4" />}
      className="bg-gradient-to-br from-primary/10 to-card"
    >
      <div className="flex flex-col gap-3">
        <div className="flex items-end gap-3">
          <p className="text-4xl font-extrabold tracking-tight text-card-foreground">
            {todayWord.word}
          </p>
          {variant === 'student' && (
            <span className="mb-1 text-sm font-medium text-muted-foreground">
              {todayWord.pronunciation}
            </span>
          )}
        </div>
        <p className="text-lg font-semibold text-primary">{todayWord.meaning}</p>
        <div className="rounded-2xl bg-muted/60 p-3">
          <p className="text-sm font-medium text-card-foreground">{todayWord.example}</p>
          <p className="mt-1 text-xs text-muted-foreground">{todayWord.exampleMeaning}</p>
        </div>

        <div className="mt-1 flex flex-wrap gap-2">
          {variant === 'teacher' ? (
            <>
              <Button size="lg" className="rounded-full">
                <Pencil /> 단어 수정
              </Button>
              <Button size="lg" variant="outline" className="rounded-full">
                <Eye /> 학생 화면 미리보기
              </Button>
            </>
          ) : (
            <>
              <Button size="lg" className="rounded-full">
                <CheckCircle2 /> 외웠어요
              </Button>
              <Button size="lg" variant="outline" className="rounded-full">
                <Volume2 /> 예문 듣기
              </Button>
            </>
          )}
        </div>
      </div>
    </DashboardCard>
  )
}
