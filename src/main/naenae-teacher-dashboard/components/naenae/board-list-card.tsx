import { MessageSquare, MessageCircle, ArrowRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DashboardCard } from './dashboard-card'
import { StatusBadge } from './status-badge'
import { boardPosts } from '@/lib/naenae-data'

export function BoardListCard({ href = '#' }: { href?: string }) {
  return (
    <DashboardCard
      title="게시판"
      icon={<MessageSquare className="size-4" />}
      action={
        <Button size="sm" variant="ghost" className="rounded-full text-primary">
          전체 보기 <ArrowRight />
        </Button>
      }
    >
      <ul className="flex flex-col divide-y divide-border">
        {boardPosts.map((post) => (
          <li key={post.title} className="flex items-center gap-3 py-3 first:pt-0 last:pb-0">
            <StatusBadge tone={post.tone}>{post.badge}</StatusBadge>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-card-foreground">{post.title}</p>
              <p className="text-xs text-muted-foreground">
                {post.author} · {post.time}
              </p>
            </div>
            <span className="flex items-center gap-1 text-xs text-muted-foreground">
              <MessageCircle className="size-3.5" />
              {post.comments}
            </span>
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
