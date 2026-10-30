const API_URL = import.meta.env.VITE_API_URL || ''

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || body?.error || `Request failed (${response.status})`)
  }

  if (response.status === 204) return null
  return response.json()
}

export const api = {
  accounts: () => request('/api/accounts'),
  createAccount: (body) => request('/api/accounts', { method: 'POST', body: JSON.stringify(body) }),
  transactions: () => request('/api/transactions'),
  createTransaction: (body) => request('/api/transactions', { method: 'POST', body: JSON.stringify(body) }),
  transfer: (body) => request('/api/transactions/transfer', { method: 'POST', body: JSON.stringify(body) }),
  chat: (message) => request('/api/chat', { method: 'POST', body: JSON.stringify({ message }) }),
}
