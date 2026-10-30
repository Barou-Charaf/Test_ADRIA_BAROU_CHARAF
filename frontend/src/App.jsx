import { useEffect, useState } from 'react'
import { api } from './api.js'

const emptyAccount = { customerReference: '', status: 'ACTIVE', currency: 'MAD', balance: '' }
const emptyTransaction = { accountId: '', amount: '', currency: 'MAD', merchantCategory: '', country: 'MA' }
const emptyTransfer = { sourceAccountId: '', destinationAccountId: '', amount: '', currency: 'MAD', merchantCategory: 'TRANSFER', country: 'MA' }

function App() {
  const [view, setView] = useState('accounts')
  const [accounts, setAccounts] = useState([])
  const [transactions, setTransactions] = useState([])
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const refresh = async () => {
    setError('')
    try {
      const [accountData, transactionData] = await Promise.all([api.accounts(), api.transactions()])
      setAccounts(accountData)
      setTransactions(transactionData)
    } catch (exception) {
      setError(exception.message)
    }
  }

  useEffect(() => { refresh() }, [])

  const runAction = async (action, message) => {
    setError('')
    setNotice('')
    try {
      await action()
      setNotice(message)
      await refresh()
      return true
    } catch (exception) {
      setError(exception.message)
      return false
    }
  }

  return (
    <div className="app-shell">
      <header>
        <div>
          <span className="eyebrow">ADRIA</span>
          <h1>Bank Console</h1>
        </div>
        <nav aria-label="Main navigation">
          {['accounts', 'transactions', 'assistant'].map((item) => (
            <button className={view === item ? 'active' : ''} onClick={() => setView(item)} key={item}>
              {item[0].toUpperCase() + item.slice(1)}
            </button>
          ))}
        </nav>
      </header>

      <main>
        {error && <div className="alert error">{error}</div>}
        {notice && <div className="alert success">{notice}</div>}
        {view === 'accounts' && <Accounts accounts={accounts} runAction={runAction} />}
        {view === 'transactions' && <Transactions transactions={transactions} accounts={accounts} runAction={runAction} />}
        {view === 'assistant' && <Assistant />}
      </main>
    </div>
  )
}

function Accounts({ accounts, runAction }) {
  const [form, setForm] = useState(emptyAccount)

  const submit = async (event) => {
    event.preventDefault()
    const done = await runAction(
      () => api.createAccount({ ...form, balance: Number(form.balance) }),
      'Account created.',
    )
    if (done) setForm(emptyAccount)
  }

  return (
    <section>
      <div className="section-heading"><div><p className="eyebrow">Overview</p><h2>Accounts</h2></div><span>{accounts.length} total</span></div>
      <div className="grid">
        <form className="card form-card" onSubmit={submit}>
          <h3>New account</h3>
          <Field label="Account reference"><input required value={form.customerReference} onChange={(e) => setForm({ ...form, customerReference: e.target.value })} /></Field>
          <div className="form-row">
            <Field label="Currency"><input required value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })} /></Field>
            <Field label="Initial balance"><input required min="0" step="0.01" type="number" value={form.balance} onChange={(e) => setForm({ ...form, balance: e.target.value })} /></Field>
          </div>
          <button className="primary" type="submit">Create account</button>
        </form>
        <div className="card table-wrap">
          <table><thead><tr><th>Reference</th><th>Status</th><th>Currency</th><th>Balance</th></tr></thead>
            <tbody>{accounts.map((account) => <tr key={account.id}><td><strong>{account.customerReference}</strong><small>{shortId(account.id)}</small></td><td><span className={`status ${account.status.toLowerCase()}`}>{account.status}</span></td><td>{account.currency}</td><td>{money(account.balance, account.currency)}</td></tr>)}</tbody>
          </table>
          {!accounts.length && <Empty text="No accounts yet." />}
        </div>
      </div>
    </section>
  )
}

function Transactions({ transactions, accounts, runAction }) {
  const [mode, setMode] = useState('payment')
  const [payment, setPayment] = useState(emptyTransaction)
  const [transfer, setTransfer] = useState(emptyTransfer)

  const submit = async (event) => {
    event.preventDefault()
    const isTransfer = mode === 'transfer'
    const body = isTransfer ? transfer : payment
    const done = await runAction(
      () => isTransfer ? api.transfer({ ...body, amount: Number(body.amount) }) : api.createTransaction({ ...body, amount: Number(body.amount) }),
      isTransfer ? 'Transfer completed.' : 'Transaction created.',
    )
    if (done) isTransfer ? setTransfer(emptyTransfer) : setPayment(emptyTransaction)
  }

  const form = mode === 'transfer' ? transfer : payment
  const setForm = mode === 'transfer' ? setTransfer : setPayment

  return (
    <section>
      <div className="section-heading"><div><p className="eyebrow">Activity</p><h2>Transactions</h2></div><span>{transactions.length} recorded</span></div>
      <div className="grid">
        <form className="card form-card" onSubmit={submit}>
          <div className="segmented"><button type="button" className={mode === 'payment' ? 'selected' : ''} onClick={() => setMode('payment')}>Payment</button><button type="button" className={mode === 'transfer' ? 'selected' : ''} onClick={() => setMode('transfer')}>Transfer</button></div>
          <AccountSelect label={mode === 'transfer' ? 'Source account' : 'Account'} value={mode === 'transfer' ? form.sourceAccountId : form.accountId} accounts={accounts} onChange={(value) => setForm({ ...form, [mode === 'transfer' ? 'sourceAccountId' : 'accountId']: value })} />
          {mode === 'transfer' && <AccountSelect label="Destination account" value={form.destinationAccountId} accounts={accounts} onChange={(value) => setForm({ ...form, destinationAccountId: value })} />}
          <div className="form-row"><Field label="Amount"><input required min="0.01" max="1000" step="0.01" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} /></Field><Field label="Currency"><input required value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })} /></Field></div>
          <Field label="Merchant category"><input required value={form.merchantCategory} onChange={(e) => setForm({ ...form, merchantCategory: e.target.value })} /></Field>
          <Field label="Country"><input required value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value.toUpperCase() })} /></Field>
          <button className="primary" type="submit">{mode === 'transfer' ? 'Transfer funds' : 'Create transaction'}</button>
        </form>
        <div className="card table-wrap"><table><thead><tr><th>Account</th><th>Category</th><th>Amount</th><th>Date</th></tr></thead><tbody>{transactions.map((transaction) => <tr key={transaction.id}><td>{shortId(transaction.accountId)}</td><td>{transaction.merchantCategory}</td><td>{money(transaction.amount, transaction.currency)}</td><td>{new Date(transaction.createdAt).toLocaleString()}</td></tr>)}</tbody></table>{!transactions.length && <Empty text="No transactions yet." />}</div>
      </div>
    </section>
  )
}

function Assistant() {
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState([{ role: 'assistant', text: 'How can I help with your accounts or transactions?' }])
  const [waiting, setWaiting] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    if (!message.trim() || waiting) return
    const text = message.trim()
    setMessage('')
    setMessages((items) => [...items, { role: 'user', text }])
    setWaiting(true)
    try {
      const response = await api.chat(text)
      setMessages((items) => [...items, { role: 'assistant', text: response.answer }])
    } catch (exception) {
      setMessages((items) => [...items, { role: 'assistant', text: exception.message }])
    } finally {
      setWaiting(false)
    }
  }

  return <section className="chat-section"><div className="section-heading"><div><p className="eyebrow">OpenAI + MCP</p><h2>Assistant</h2></div></div><div className="card chat-card"><div className="messages">{messages.map((item, index) => <div className={`message ${item.role}`} key={`${item.role}-${index}`}>{item.text}</div>)}{waiting && <div className="message assistant muted">Thinking…</div>}</div><form className="chat-form" onSubmit={submit}><input aria-label="Message" placeholder="Ask about an account, payment or transfer…" value={message} onChange={(e) => setMessage(e.target.value)} /><button className="primary" disabled={waiting}>Send</button></form></div></section>
}

function AccountSelect({ label, value, accounts, onChange }) { return <Field label={label}><select required value={value} onChange={(e) => onChange(e.target.value)}><option value="">Select an account</option>{accounts.map((account) => <option value={account.id} key={account.id}>{account.customerReference} · {account.currency} {account.balance}</option>)}</select></Field> }
function Field({ label, children }) { return <label><span>{label}</span>{children}</label> }
function Empty({ text }) { return <p className="empty">{text}</p> }
function shortId(value) { return value ? `${value.slice(0, 8)}…` : '—' }
function money(value, currency) { return new Intl.NumberFormat(undefined, { style: 'currency', currency: currency || 'MAD' }).format(Number(value || 0)) }

export default App
