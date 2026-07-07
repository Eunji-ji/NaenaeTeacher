import { SmileCharacter } from './smile-character'

export function WelcomeCard({
  greeting,
  message,
}: {
  greeting: string
  message: string
}) {
  return (
    <section className="relative overflow-hidden rounded-3xl border border-border bg-gradient-to-br from-primary/15 via-card to-accent/25 p-6 shadow-sm sm:p-7">
      <div className="flex items-center gap-4">
        <div className="hidden sm:block">
          <SmileCharacter size={64} />
        </div>
        <div className="min-w-0">
          <div className="flex items-center gap-2 sm:hidden">
            <SmileCharacter size={36} />
          </div>
          <h2 className="mt-2 text-xl font-extrabold text-card-foreground text-balance sm:mt-0 sm:text-2xl">
            {greeting}
          </h2>
          <p className="mt-1 text-sm leading-relaxed text-muted-foreground text-pretty">
            {message}
          </p>
        </div>
      </div>
    </section>
  )
}
