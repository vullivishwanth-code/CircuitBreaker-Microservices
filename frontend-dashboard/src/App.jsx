import { useState } from 'react'
import './App.css'

const services = [
  {
    name: 'API Gateway',
    port: '8080',
    description: 'Single entry point for client requests',
    icon: '⇄',
  },
  {
    name: 'Service Registry',
    port: '8761',
    description: 'Eureka service discovery',
    icon: '◎',
  },
  {
    name: 'Product Service',
    port: '8081',
    description: 'Product information and aggregation',
    icon: '▣',
  },
  {
    name: 'Inventory Service',
    port: '8082',
    description: 'Product inventory availability',
    icon: '▤',
  },
  {
    name: 'Recommendation Service',
    port: '8083',
    description: 'Product recommendations',
    icon: '◇',
  },
]

const resiliencePatterns = [
  {
    title: 'Circuit Breaker',
    value: 'CLOSED',
    description: 'Protects services from cascading failures',
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

function App() {
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const sendRequest = async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await fetch('/api/products/1/details')

      if (!result.ok) {
        throw new Error(`Request failed with HTTP ${result.status}`)
      }

      const data = await result.json()
      setResponse(data)
    } catch (err) {
      setResponse(null)
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

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
          <div className="environment-dot"></div>

          <div>
            <strong>Local Environment</strong>
            <span>Spring Boot + React</span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">MICROSERVICES RELIABILITY PLATFORM</p>
            <h1>Circuit Breaker Dashboard</h1>
          </div>

          <div className="system-status">
            <span className="status-dot"></span>
            System Ready
          </div>
        </header>

        <section className="hero-section" id="overview">
          <div>
            <span className="section-label">SYSTEM OVERVIEW</span>

            <h2>
              Resilient microservices.
              <br />
              Visible in one place.
            </h2>

            <p>
              Monitor the Spring Boot microservices architecture and demonstrate
              circuit breakers, rate limiting, bulkhead isolation and timeout
              protection from a single dashboard.
            </p>

            <div className="hero-actions">
              <button className="primary-button">Run Health Check</button>

              <button
                className="secondary-button"
                onClick={sendRequest}
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
              <div className="architecture-node client">Client</div>

              <span className="flow-arrow">↓</span>

              <div className="architecture-node gateway">
                API Gateway :8080
              </div>

              <span className="flow-arrow">↓</span>

              <div className="service-flow">
                <div className="architecture-node">Product</div>
                <div className="architecture-node">Inventory</div>
                <div className="architecture-node">Recommendation</div>
              </div>

              <span className="flow-arrow">↓</span>

              <div className="architecture-node registry">
                Eureka Registry :8761
              </div>
            </div>
          </div>
        </section>

        <section className="dashboard-section" id="services">
          <div className="section-heading">
            <div>
              <span className="section-label">INFRASTRUCTURE</span>
              <h2>Microservices</h2>
            </div>

            <span className="service-count">
              {services.length} services
            </span>
          </div>

          <div className="services-grid">
            {services.map((service) => (
              <article className="service-card" key={service.name}>
                <div className="service-card-top">
                  <div className="service-icon">
                    {service.icon}
                  </div>

                  <span className="service-status">
                    <span></span>
                    CONFIGURED
                  </span>
                </div>

                <h3>{service.name}</h3>

                <p>{service.description}</p>

                <div className="service-meta">
                  <span>PORT</span>
                  <strong>{service.port}</strong>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="dashboard-section" id="resilience">
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

        <section className="dashboard-section" id="requests">
          <div className="section-heading">
            <div>
              <span className="section-label">TESTING</span>
              <h2>Request Console</h2>
            </div>
          </div>

          <div className="request-console">
            <div>
              <span className="request-method">GET</span>

              <code>/api/products/1/details</code>
            </div>

            <button
              className="primary-button"
              onClick={sendRequest}
              disabled={loading}
            >
              {loading ? 'Sending...' : 'Send Request'}
            </button>
          </div>

          <div className="response-placeholder">
            <span>RESPONSE</span>

            {loading && (
              <p>Sending request to API Gateway...</p>
            )}

            {error && (
              <p>ERROR: {error}</p>
            )}

            {response && (
              <pre>
                {JSON.stringify(response, null, 2)}
              </pre>
            )}

            {!loading && !error && !response && (
              <p>
                Click Send Request to test the API Gateway.
              </p>
            )}
          </div>
        </section>

        <footer>
          CircuitBreaker Microservices • Spring Boot • React • Resilience4j
        </footer>
      </main>
    </div>
  )
}

export default App