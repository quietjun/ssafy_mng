// Interactive Mouse Cursor & Trail Particle Manager
type CursorType = 
  | 'cursor-default' 
  | 'cursor-magic-star' 
  | 'cursor-cyber-crosshair' 
  | 'cursor-pixel-sword' 
  | 'cursor-cat-paw' 
  | 'cursor-fire-flame' 
  | 'cursor-code-terminal' 
  | 'cursor-bubble-pop'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  color: string
  alpha: number
  life: number
  maxLife: number
  text?: string
  rotation?: number
  vRot?: number
}

class CursorManager {
  private currentCursor: string = 'cursor-default'
  private canvas: HTMLCanvasElement | null = null
  private ctx: CanvasRenderingContext2D | null = null
  private particles: Particle[] = []
  private isRunning: boolean = false
  private animId: number | null = null
  private lastX: number = 0
  private lastY: number = 0
  private lastSpawnTime: number = 0

  constructor() {
    if (typeof window !== 'undefined') {
      window.addEventListener('mousemove', this.handleMouseMove.bind(this), { passive: true })
      window.addEventListener('resize', this.handleResize.bind(this), { passive: true })
    }
  }

  private initCanvas() {
    if (this.canvas) return
    this.canvas = document.createElement('canvas')
    this.canvas.id = 'custom-cursor-canvas'
    this.canvas.style.position = 'fixed'
    this.canvas.style.top = '0'
    this.canvas.style.left = '0'
    this.canvas.style.width = '100vw'
    this.canvas.style.height = '100vh'
    this.canvas.style.pointerEvents = 'none'
    this.canvas.style.zIndex = '999999'
    document.body.appendChild(this.canvas)

    this.ctx = this.canvas.getContext('2d')
    this.handleResize()
  }

  private handleResize() {
    if (!this.canvas) return
    const dpr = window.devicePixelRatio || 1
    this.canvas.width = window.innerWidth * dpr
    this.canvas.height = window.innerHeight * dpr
    if (this.ctx) {
      this.ctx.scale(dpr, dpr)
    }
  }

  public setCursor(cursorId: string) {
    this.currentCursor = cursorId || 'cursor-default'
    if (typeof document !== 'undefined') {
      if (this.currentCursor === 'cursor-default') {
        document.documentElement.removeAttribute('data-cursor')
      } else {
        document.documentElement.setAttribute('data-cursor', this.currentCursor)
      }
    }

    if (this.currentCursor !== 'cursor-default') {
      this.initCanvas()
      if (!this.isRunning) {
        this.isRunning = true
        this.loop()
      }
    }
  }

  private handleMouseMove(e: MouseEvent) {
    if (this.currentCursor === 'cursor-default') return

    const now = performance.now()
    const dx = e.clientX - this.lastX
    const dy = e.clientY - this.lastY
    const dist = Math.hypot(dx, dy)

    if (dist > 4 && now - this.lastSpawnTime > 16) {
      this.spawnParticles(e.clientX, e.clientY)
      this.lastX = e.clientX
      this.lastY = e.clientY
      this.lastSpawnTime = now
    }
  }

  private spawnParticles(x: number, y: number) {
    const type = this.currentCursor as CursorType
    const count = type === 'cursor-cat-paw' ? 1 : 2

    for (let i = 0; i < count; i++) {
      let p: Particle

      switch (type) {
        case 'cursor-magic-star': {
          const colors = ['#fde047', '#f472b6', '#38bdf8', '#c084fc', '#ffffff']
          p = {
            x: x + (Math.random() - 0.5) * 8,
            y: y + (Math.random() - 0.5) * 8,
            vx: (Math.random() - 0.5) * 1.5,
            vy: Math.random() * 1.5 + 0.5,
            size: Math.random() * 5 + 3,
            color: colors[Math.floor(Math.random() * colors.length)],
            alpha: 1,
            life: 0,
            maxLife: Math.random() * 20 + 25,
            text: Math.random() > 0.4 ? '✦' : '★',
            rotation: Math.random() * Math.PI * 2,
            vRot: (Math.random() - 0.5) * 0.1
          }
          break
        }
        case 'cursor-cyber-crosshair': {
          const colors = ['#00f0ff', '#ff007f', '#38bdf8', '#00ffcc']
          p = {
            x: x + (Math.random() - 0.5) * 6,
            y: y + (Math.random() - 0.5) * 6,
            vx: (Math.random() - 0.5) * 2,
            vy: (Math.random() - 0.5) * 2,
            size: Math.random() * 4 + 2,
            color: colors[Math.floor(Math.random() * colors.length)],
            alpha: 1,
            life: 0,
            maxLife: 24,
            rotation: Math.random() * Math.PI,
            vRot: 0.1
          }
          break
        }
        case 'cursor-pixel-sword': {
          const colors = ['#fbbf24', '#f59e0b', '#60a5fa', '#f8fafc']
          p = {
            x: x + (Math.random() - 0.5) * 10,
            y: y + (Math.random() - 0.5) * 10,
            vx: (Math.random() - 0.5) * 2,
            vy: Math.random() * 2,
            size: Math.floor(Math.random() * 4) + 3, // 픽셀 느낌 사각형
            color: colors[Math.floor(Math.random() * colors.length)],
            alpha: 1,
            life: 0,
            maxLife: 28
          }
          break
        }
        case 'cursor-cat-paw': {
          p = {
            x: x + (Math.random() - 0.5) * 6,
            y: y + (Math.random() - 0.5) * 6,
            vx: (Math.random() - 0.5) * 0.5,
            vy: -0.8 - Math.random() * 0.5,
            size: Math.random() * 4 + 14,
            color: '#f472b6',
            alpha: 0.95,
            life: 0,
            maxLife: 35,
            text: '🐾',
            rotation: (Math.random() - 0.5) * 0.4
          }
          break
        }
        case 'cursor-fire-flame': {
          const colors = ['#ef4444', '#f97316', '#fbbf24', '#fde047']
          p = {
            x: x + (Math.random() - 0.5) * 8,
            y: y + (Math.random() - 0.5) * 8,
            vx: (Math.random() - 0.5) * 1.8,
            vy: -Math.random() * 2.5 - 1.0,
            size: Math.random() * 6 + 4,
            color: colors[Math.floor(Math.random() * colors.length)],
            alpha: 1,
            life: 0,
            maxLife: Math.random() * 20 + 20
          }
          break
        }
        case 'cursor-code-terminal': {
          const bits = ['1', '0', '{', '}', ';', '>', '<', 'λ']
          p = {
            x: x + (Math.random() - 0.5) * 10,
            y: y + (Math.random() - 0.5) * 10,
            vx: (Math.random() - 0.5) * 1.2,
            vy: Math.random() * 1.6 + 0.4,
            size: Math.random() * 3 + 11,
            color: '#22c55e',
            alpha: 1,
            life: 0,
            maxLife: 30,
            text: bits[Math.floor(Math.random() * bits.length)]
          }
          break
        }
        case 'cursor-bubble-pop': {
          p = {
            x: x + (Math.random() - 0.5) * 12,
            y: y + (Math.random() - 0.5) * 12,
            vx: (Math.random() - 0.5) * 1.5,
            vy: -Math.random() * 1.8 - 0.6,
            size: Math.random() * 6 + 5,
            color: '#38bdf8',
            alpha: 0.85,
            life: 0,
            maxLife: 35,
            text: '🫧'
          }
          break
        }
        default:
          return
      }

      this.particles.push(p)
    }

    if (this.particles.length > 80) {
      this.particles.splice(0, this.particles.length - 80)
    }
  }

  private loop() {
    if (!this.ctx || !this.canvas) return

    this.ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)

    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i]
      p.life++
      p.x += p.vx
      p.y += p.vy
      if (p.rotation !== undefined && p.vRot) {
        p.rotation += p.vRot
      }

      const progress = p.life / p.maxLife
      p.alpha = Math.max(0, 1 - progress)

      if (p.life >= p.maxLife) {
        this.particles.splice(i, 1)
        continue
      }

      this.ctx.save()
      this.ctx.globalAlpha = p.alpha

      if (p.text) {
        this.ctx.font = `${p.size}px sans-serif`
        this.ctx.fillStyle = p.color
        this.ctx.textAlign = 'center'
        this.ctx.textBaseline = 'middle'
        this.ctx.translate(p.x, p.y)
        if (p.rotation) this.ctx.rotate(p.rotation)
        this.ctx.fillText(p.text, 0, 0)
      } else if (this.currentCursor === 'cursor-pixel-sword') {
        this.ctx.fillStyle = p.color
        this.ctx.fillRect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size)
      } else {
        this.ctx.beginPath()
        this.ctx.arc(p.x, p.y, p.size * (1 - progress * 0.4), 0, Math.PI * 2)
        this.ctx.fillStyle = p.color
        this.ctx.shadowColor = p.color
        this.ctx.shadowBlur = 6
        this.ctx.fill()
      }

      this.ctx.restore()
    }

    this.animId = requestAnimationFrame(this.loop.bind(this))
  }
}

export const cursorManager = new CursorManager()
