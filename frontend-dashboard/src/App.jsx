import { useState } from 'react'
import './App.css'

const services = [
  {
    key: 'gateway',
    name: 'API Gateway',
    port: '8080',
    description: 'Single entry point for client requests',
    icon: '⇄',
    healthUrl: '/health/gateway/api/products/1/details',
  },
  {
    key: 'registry',
    name: 'Service Registry',
    port: '8761',
    description: 'Eureka service discovery',
    icon: '◎',
    healthUrl: '/health/registry/',
  },
  {
    key: 'product',
    name: 'Product Service',
    port: '8081',
    description: 'Product information and aggregation',
    icon: '▣',
    healthUrl: '/health/product/api/products/1',
  },
  {
    key: 'inventory',
    name: 'Inventory Service',
    port: '8082',
    description: 'Product inventory availability',
    icon: '▤',
    healthUrl: '/health/inventory/api/inventory/1',
  },
  {
    key: 'recommendation',
    name: 'Recommendation Service',
    port: '8083',
    description: 'Product recommendations',
    icon: '◇',
    healthUrl: '/health/recommendation/api/recommendations/1',
  },
]

const initialHealth = {
  gateway: 'UNKNOWN',
  registry: 'UNKNOWN',
  product: 'UNKNOWN',
  inventory: 'UNKNOWN',
  recommendation: 'UNKNOWN',
}

function App() {
  const [response, setResponse] = useState(null)
  const [requestLoading, setRequestLoading] = useState(false)
  const [requestError, setRequestError] = useState('')

  const [serviceHealth, setServiceHealth] = useState(initialHealth)
  const [healthChecking, setHealthChecking] = useState(false)
  const [lastChecked, setLastChecked] = useState(null)

  const [circuitBreakerState, setCircuitBreakerState] =
    useState('UNKNOWN')

  const sendRequest = async () => {
    setRequestLoading(true)
    setRequestError('')
    setResponse(null)

    try {
      const result = await fetch('/api/products/1/details')

      if (!result.ok) {
        throw new Error(`Request failed with HTTP ${result.status}`)
      }

      const data = await result.json()
      setResponse(data)
    } catch (error) {
      setRequestError(error.message)
    } finally {
      setRequestLoading(false)
    }
  }

  const checkService = async (service) => {
    const controller = new AbortController()

    const timeout = setTimeout(() => {
      controller.abort()
    }, 5000)

    try {
      const result = await fetch(service.healthUrl, {
        method: 'GET',
        signal: controller.signal,
        cache: 'no-store',
      })

      clearTimeout(timeout)

      return result.ok ? 'UP' : 'DOWN'
    } catch {
      clearTimeout(timeout)
      return 'DOWN'
    }
  }

  const fetchCircuitBreakerState = async () => {
    const states = ['closed', 'open', 'half_open']

    try {
      for (const state of states) {
        const result = await fetch(
          `/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:productGatewayCircuitBreaker&tag=state:${state}`,
          {
            cache: 'no-store',
          },
        )

        if (!result.ok) {
          continue
        }

        const data = await result.json()
        const value = data.measurements?.[0]?.value ?? 0

        if (value === 1) {
          setCircuitBreakerState(state.toUpperCase())
          return
        }
      }

      setCircuitBreakerState('UNKNOWN')
    } catch {
      setCircuitBreakerState('UNKNOWN')
    }
  }

  const runHealthCheck = async () => {
    setHealthChecking(true)

    const checkingState = {}

    services.forEach((service) => {
      checkingState[service.key] = 'CHECKING'
    })

    setServiceHealth(checkingState)

    const results = await Promise.all(
      services.map(async (service) => {
        const status = await checkService(service)

        return {
          key: service.key,
          status,
        }
      }),
    )

    const updatedHealth = {}

    results.forEach((result) => {
      updatedHealth[result.key] = result.status
    })

    setServiceHealth(updatedHealth)

    await fetchCircuitBreakerState()

    setLastChecked(new Date())
    setHealthChecking(false)
  }

  const scrollToRequests = () => {
    document
      .getElementById('requests')
      ?.scrollIntoView({ behavior: 'smooth' })
  }

  const allServicesUp = services.every(
    (service) => serviceHealth[service.key] === 'UP',
  )

  const hasDownService = services.some(
    (service) => serviceHealth[service.key] === 'DOWN',
  )

  const downServices = services.filter(
    (service) => serviceHealth[service.key] === 'DOWN',
  )

  const getSystemStatus = () => {
    if (healthChecking) {
      return 'Checking Services...'
    }

    if (hasDownService) {
      return 'Service Degradation Detected'
    }

    if (allServicesUp) {
      return 'All Systems Operational'
    }

    return 'System Ready'
  }

  const resiliencePatterns = [
    {
      title: 'Circuit Breaker',
      value: circuitBreakerState,
      description: 'Live Product Gateway circuit breaker state',
    },
    {
      title: 'Rate Limiter',
      value: '5 / 10 sec',
      description: 'Controls excessive incoming requests',
    },
    {
      title: 'Bulkhead',
      value: '3 concurrent',
      description: 'Isolates concurrent service requests',
    },
    {
      title: 'Time Limiter',
      value: '3 seconds',
      description: 'Stops requests that take too long',
    },
  ]

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">CB</div>

          <div>
            <h2>ResilienceHub</h2>
            <span>Microservices Monitor</span>
          </div>
        </div>

        <nav className="navigation">
          <a className="nav-item active" href="#overview">
            <span>▦</span>
            Overview
          </a>

          <a className="nav-item" href="#services">
            <span>◫</span>
            Services
          </a>

          <a className="nav-item" href="#resilience">
            <span>⌁</span>
            Resilience
          </a>

          <a className="nav-item" href="#requests">
            <span>↗</span>
            Requests
          </a>
        </nav>

        <div className="sidebar-footer">
          <div
            className={`environment-dot ${
              hasDownService ? 'environment-down' : ''
            }`}
          ></div>

          <div>
            <strong>Local Environment</strong>
            <span>Spring Boot + React</span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">
              MICROSERVICES RELIABILITY PLATFORM
            </p>

            <h1>Circuit Breaker Dashboard</h1>
          </div>

          <div
            className={`system-status ${
              hasDownService ? 'system-status-error' : ''
            }`}
          >
            <span
              className={`status-dot ${
                hasDownService ? 'status-dot-error' : ''
              }`}
            ></span>

            {getSystemStatus()}
          </div>
        </header>

        {lastChecked && (
          <div
            className={`health-summary ${
              hasDownService ? 'health-summary-error' : ''
            }`}
          >
            <div>
              <strong>
                {hasDownService
                  ? `${downServices.length} service${
                      downServices.length > 1 ? 's' : ''
                    } unavailable`
                  : 'All monitored services are responding'}
              </strong>

              <span>
                Last checked at {lastChecked.toLocaleTimeString()}
              </span>
            </div>

            {hasDownService && (
              <div className="failed-services">
                {downServices.map((service) => (
                  <span key={service.key}>
                    {service.name} DOWN
                  </span>
                ))}
              </div>
            )}
          </div>
        )}

        <section className="hero-section" id="overview">
          <div>
            <span className="section-label">
              SYSTEM OVERVIEW
            </span>

            <h2>
              Resilient microservices.
              <br />
              Visible in one place.
            </h2>

            <p>
              Monitor the Spring Boot microservices architecture and
              demonstrate circuit breakers, rate limiting, bulkhead
              isolation and timeout protection from a single dashboard.
            </p>

            <div className="hero-actions">
              <button
                className="primary-button"
                onClick={runHealthCheck}
                disabled={healthChecking}
              >
                {healthChecking
                  ? 'Checking...'
                  : 'Run Health Check'}
              </button>

              <button
                className="secondary-button"
                onClick={scrollToRequests}
              >
                Send Test Request
              </button>
            </div>
          </div>

          <div className="architecture-preview">
            <div className="architecture-title">
              <span>Architecture</span>
              <small>LOCAL</small>
            </div>

            <div className="architecture-flow">
              <div className="architecture-node client">
                Client
              </div>

              <span className="flow-arrow">↓</span>

              <div className="architecture-node gateway">
                API Gateway :8080
              </div>

              <span className="flow-arrow">↓</span>

              <div className="service-flow">
                <div className="architecture-node">
                  Product
                </div>

                <div className="architecture-node">
                  Inventory
                </div>

                <div className="architecture-node">
                  Recommendation
                </div>
              </div>

              <span className="flow-arrow">↓</span>

              <div className="architecture-node registry">
                Eureka Registry :8761
              </div>
            </div>
          </div>
        </section>

        <section
          className="dashboard-section"
          id="services"
        >
          <div className="section-heading">
            <div>
              <span className="section-label">
                INFRASTRUCTURE
              </span>

              <h2>Microservices</h2>
            </div>

            <span className="service-count">
              {services.length} services
            </span>
          </div>

          <div className="services-grid">
            {services.map((service) => {
              const status =
                serviceHealth[service.key] || 'UNKNOWN'

              return (
                <article
                  className={`service-card ${
                    status === 'DOWN'
                      ? 'service-card-down'
                      : ''
                  }`}
                  key={service.name}
                >
                  <div className="service-card-top">
                    <div className="service-icon">
                      {service.icon}
                    </div>

                    <span
                      className={`service-status service-status-${status.toLowerCase()}`}
                    >
                      <span></span>
                      {status}
                    </span>
                  </div>

                  <h3>{service.name}</h3>

                  <p>{service.description}</p>

                  <div className="service-meta">
                    <span>PORT</span>
                    <strong>{service.port}</strong>
                  </div>
                </article>
              )
            })}
          </div>
        </section>

        <section
          className="dashboard-section"
          id="resilience"
        >
          <div className="section-heading">
            <div>
              <span className="section-label">
                RESILIENCE4J
              </span>

              <h2>Resilience Patterns</h2>
            </div>
          </div>

          <div className="resilience-grid">
            {resiliencePatterns.map((pattern) => (
              <article
                className="resilience-card"
                key={pattern.title}
              >
                <div className="pattern-header">
                  <h3>{pattern.title}</h3>
                  <span>●</span>
                </div>

                <strong>{pattern.value}</strong>

                <p>{pattern.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section
          className="dashboard-section"
          id="requests"
        >
          <div className="section-heading">
            <div>
              <span className="section-label">
                TESTING
              </span>

              <h2>Request Console</h2>
            </div>
          </div>

          <div className="request-console">
            <div>
              <span className="request-method">
                GET
              </span>

              <code>/api/products/1/details</code>
            </div>

            <button
              className="primary-button"
              onClick={sendRequest}
              disabled={requestLoading}
            >
              {requestLoading
                ? 'Sending...'
                : 'Send Request'}
            </button>
          </div>

          <div className="response-placeholder">
            <span>RESPONSE</span>

            {requestLoading && (
              <p>
                Waiting for API Gateway response...
              </p>
            )}

            {!requestLoading &&
              !requestError &&
              !response && (
                <p>
                  Click Send Request to test the real API
                  Gateway endpoint.
                </p>
              )}

            {requestError && (
              <p className="request-error">
                ERROR: {requestError}
              </p>
            )}

            {response && (
              <pre className="response-json">
                {JSON.stringify(response, null, 2)}
              </pre>
            )}
          </div>
        </section>

        <footer>
          CircuitBreaker Microservices • Spring Boot • React •
          Resilience4j
        </footer>
      </main>
    </div>
  )
}

export default App