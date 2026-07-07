import { MessageCircleHeart, Pencil, Copy, Mic, CheckCircle2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { todaySentence } from '@/lib/naenae-data'

export function TodaySentenceCard({ variant }: { variant: 'teacher' | 'student' }) {
  return (
    <DashboardCard
      title="오늘의 문장"
      icon={<MessageCircleHeart className="size-4" />}
      className="bg-gradient-to-br from-accent/40 to-card"
    >
      <div className="flex flex-col gap-3">
        <div className="relative rounded-2xl bg-card p-4 shadow-sm">
          <span className="absolute -top-2 left-4 text-2xl leading-none" aria-hidden>
            💬
          </span>
          <p className="text-xl font-bold leading-relaxed text-card-foreground text-pretty">
            {todaySentence.sentence}
          </p>
          <p className="mt-1 text-sm text-muted-foreground">{todaySentence.meaning}</p>
        </div>

        <div className="flex flex-wrap gap-2">
          {variant === 'teacher' ? (
            <>
              <Button size="lg" className="rounded-full">
                <Pencil /> 문장 수정
              </Button>
              <Button size="lg" variant="outline" className="rounded-full">
                <Copy /> 복사
              </Button>
            </>
          ) : (
            <>
              <Button size="lg" className="rounded-full">
                <Mic /> 따라 말하기
              </Button>
              <Button size="lg" variant="outline" className="rounded-full">
                <CheckCircle2 /> 복습 완료
              </Button>
            </>
          )}
        </div>
      </div>
    </DashboardCard>
  )
}
