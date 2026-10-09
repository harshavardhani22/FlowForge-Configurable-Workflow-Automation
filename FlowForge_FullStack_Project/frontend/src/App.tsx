import { useEffect, useMemo, useState } from 'react'
import { Activity, ArrowDownRight, ArrowRight, Bell, Check, CheckCircle2, ChevronDown, CircleHelp, Clock3, FileCheck2, GitBranch, LayoutDashboard, ListTodo, Plus, RefreshCw, Search, Settings2, ShieldCheck, Workflow, X, Zap } from 'lucide-react'

const API = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'
const credentials = btoa('demo:demo123')
type Definition = { id:number; name:string; description:string; stages:{id:number;name:string;requiredRole:string}[] }
type Instance = { id:number; title:string; status:string; currentStage:string; createdAt:string; definition:{id:number;name:string}; context:Record<string,unknown> }
type Task = { id:number; stageName:string; requiredRole:string; status:string; createdAt:string; instance:{id:number;title:string;status:string;definition:{name:string}}; lastComment?:string }
type Audit = {id:number;eventType:string;actor:string;details:string;occurredAt:string}
async function api<T>(path:string, options:RequestInit={}):Promise<T>{
  const res = await fetch(`${API}${path}`, { ...options, headers:{'Content-Type':'application/json','Authorization':`Basic ${credentials}`,...(options.headers||{})} })
  if(!res.ok){ const body=await res.text(); throw new Error(body || `Request failed (${res.status})`) }
  return res.json()
}
const date = (s?:string) => s ? new Date(s).toLocaleString([], {month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) : '—'
const shortStatus = (s:string) => s?.toLowerCase().replaceAll('_',' ') || 'unknown'

export default function App(){
  const [tab,setTab]=useState('Overview')
  const [definitions,setDefinitions]=useState<Definition[]>([])
  const [instances,setInstances]=useState<Instance[]>([])
  const [tasks,setTasks]=useState<Task[]>([])
  const [timeline,setTimeline]=useState<Audit[]>([])
  const [selected,setSelected]=useState<Instance|null>(null)
  const [loading,setLoading]=useState(true)
  const [error,setError]=useState('')
  const [query,setQuery]=useState('')
  const [showCreate,setShowCreate]=useState(false)
  const [showStart,setShowStart]=useState(false)
  const [newName,setNewName]=useState('Expense approval')
  const [newDesc,setNewDesc]=useState('Approval flow for employee expenses')
  const [title,setTitle]=useState('Laptop purchase')
  const [amount,setAmount]=useState('650')
  const [department,setDepartment]=useState('engineering')
  const [busy,setBusy]=useState(false)

  async function load(){
    setLoading(true); setError('')
    try {
      const [d,i,t]=await Promise.all([api<Definition[]>('/definitions'),api<Instance[]>('/instances'),api<Task[]>('/tasks')])
      setDefinitions(d);setInstances(i);setTasks(t)
    } catch(e){setError(e instanceof Error ? e.message : 'Could not load API data')} finally{setLoading(false)}
  }
  useEffect(()=>{load()},[])
  async function openInstance(i:Instance){
    setSelected(i);setTab('Timeline')
    try{setTimeline(await api<Audit[]>(`/instances/${i.id}/timeline`))}catch(e){setError(e instanceof Error?e.message:'Could not load timeline')}
  }
  async function createDefinition(){
    setBusy(true);setError('')
    try{
      await api('/definitions',{method:'POST',body:JSON.stringify({name:newName,description:newDesc,stages:[
        {name:'Manager review',requiredRole:'MANAGER',onApprove:'Finance review',onReject:'REJECTED',onReturn:'RETURNED',onEscalate:'Finance review',conditionKey:'amount',conditionOperator:'GT',conditionValue:'1000',conditionTarget:'Finance review'},
        {name:'Finance review',requiredRole:'FINANCE',onApprove:'APPROVED',onReject:'REJECTED',onReturn:'Manager review',onEscalate:'ADMIN'}
      ]})})
      setShowCreate(false);await load()
    }catch(e){setError(e instanceof Error?e.message:'Could not create workflow')}finally{setBusy(false)}
  }
  async function startInstance(){
    if(!definitions.length){setError('Create a workflow definition first.');return}
    setBusy(true);setError('')
    try{
      await api('/instances',{method:'POST',body:JSON.stringify({definitionId:definitions[0].id,title,context:{amount:Number(amount),department}})})
      setShowStart(false);await load()
    }catch(e){setError(e instanceof Error?e.message:'Could not start workflow')}finally{setBusy(false)}
  }
  async function act(task:Task,action:string){
    setError('')
    try{
      await api(`/tasks/${task.id}/actions`,{method:'POST',body:JSON.stringify({action,comment:action==='APPROVE'?'Reviewed in FlowForge':'Action recorded in FlowForge',actor:'demo',idempotencyKey:`ui-${task.id}-${action}-${Date.now()}`})})
      await load()
      if(selected) await openInstance(selected)
    }catch(e){setError(e instanceof Error?e.message:'Action failed')}
  }

  const pending=tasks.filter(t=>t.status==='PENDING')
  const running=instances.filter(i=>i.status==='RUNNING')
  const completed=instances.filter(i=>['APPROVED','REJECTED'].includes(i.status))
  const visibleInstances=useMemo(()=>instances.filter(i=>`${i.title} ${i.definition?.name} ${i.currentStage} ${i.status}`.toLowerCase().includes(query.toLowerCase())),[instances,query])
  const nav=[['Overview',LayoutDashboard],['My tasks',ListTodo],['Workflows',Workflow],['Executions',GitBranch],['Timeline',Activity]] as const

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark"><Workflow size={21}/></div><div><b>flowforge</b><span>WORKFLOW OPERATIONS</span></div></div>
      <div className="workspace"><div className="workspace-icon">N</div><div><b>Northstar Studio</b><small>Workspace</small></div><ChevronDown size={15}/></div>
      <div className="nav-label">WORKSPACE</div>
      <nav>{nav.map(([label,Icon])=><button key={label} className={`nav-item ${tab===label?'active':''}`} onClick={()=>setTab(label)}><Icon size={17}/><span>{label}</span>{label==='My tasks'&&pending.length>0&&<em>{pending.length}</em>}</button>)}</nav>
      <div className="nav-label tools-label">MANAGE</div>
      <button className="nav-item" onClick={()=>setShowCreate(true)}><Settings2 size={17}/><span>Workflow builder</span></button>
      <button className="nav-item" onClick={()=>setTab('Executions')}><ShieldCheck size={17}/><span>Audit & compliance</span></button>
      <div className="sidebar-bottom"><div className="help-card"><div className="help-icon"><CircleHelp size={18}/></div><b>Need a hand?</b><p>Explore how your workflow engine runs.</p><button onClick={()=>setError('Tip: create a workflow, start an instance, then approve a pending task.')}>View quick guide <ArrowRight size={13}/></button></div><div className="profile"><div className="avatar">D</div><div><b>Demo User</b><small>Workspace member</small></div><Settings2 size={16}/></div></div>
    </aside>
    <main className="main">
      <header className="topbar"><div className="breadcrumbs">Workspace <span>/</span> <b>{tab}</b></div><div className="top-actions"><div className="search"><Search size={15}/><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search workflows..." /></div><button className="icon-button" onClick={load} title="Refresh"><RefreshCw size={16}/></button><button className="icon-button" onClick={()=>setError('Notifications are represented by the persisted workflow timeline in this starter.')} title="Notifications"><Bell size={16}/><i/></button><div className="avatar small">D</div></div></header>
      <div className="content">
        {error&&<div className="error-banner"><span>{error}</span><button onClick={()=>setError('')}><X size={15}/></button></div>}
        {tab==='Overview'&&<>
          <div className="welcome-row"><div><div className="eyebrow"><span className="live-dot"/> WORKFLOW CONTROL CENTER</div><h1>Good afternoon, Demo User <span>✦</span></h1><p className="subtitle">Here’s what’s moving across your workspace today.</p></div><div className="header-buttons"><button className="btn secondary" onClick={()=>setShowCreate(true)}><Plus size={16}/> New workflow</button><button className="btn primary" onClick={()=>setShowStart(true)}><Zap size={16}/> Start an execution</button></div></div>
          <div className="stats-grid">
            <Stat icon={<Activity/>} label="Active executions" value={String(running.length).padStart(2,'0')} trend="In progress" tone="blue"/>
            <Stat icon={<ListTodo/>} label="Tasks awaiting you" value={String(pending.length).padStart(2,'0')} trend="Need attention" tone="amber"/>
            <Stat icon={<CheckCircle2/>} label="Resolved workflows" value={String(completed.length).padStart(2,'0')} trend="Approved or rejected" tone="green"/>
            <Stat icon={<GitBranch/>} label="Workflow templates" value={String(definitions.length).padStart(2,'0')} trend="Ready to launch" tone="purple"/>
          </div>
          <div className="dashboard-grid">
            <section className="panel execution-panel"><div className="panel-head"><div><h2>Recent executions</h2><p>Live status across your workflows</p></div><button className="text-button" onClick={()=>setTab('Executions')}>View all <ArrowRight size={14}/></button></div>
              {loading?<Empty text="Loading workflow executions…"/>:visibleInstances.length===0?<Empty text="No executions yet. Start your first workflow."/>:<div className="execution-list">{visibleInstances.slice(0,5).map((i,idx)=><button className="execution-row" key={i.id} onClick={()=>openInstance(i)}><div className={`flow-icon fi-${idx%4}`}><Workflow size={17}/></div><div className="execution-info"><b>{i.title}</b><span>{i.definition?.name||'Workflow'} · #{i.id}</span></div><div className="stage-col"><small>CURRENT STAGE</small><b>{i.currentStage||'—'}</b></div><Status value={i.status}/><div className="row-date">{date(i.createdAt)}</div><ArrowRight size={15} className="row-arrow"/></button>)}</div>}
            </section>
            <section className="panel task-panel"><div className="panel-head"><div><h2>Pending tasks</h2><p>Items waiting for a decision</p></div><span className="count-pill">{pending.length} open</span></div>
              {pending.slice(0,4).map(t=><div className="task-card" key={t.id}><div className="task-card-top"><span className="task-type"><FileCheck2 size={14}/> {t.requiredRole}</span><span className="due"><Clock3 size={12}/> {date(t.createdAt)}</span></div><b>{t.instance?.title||`Task #${t.id}`}</b><p>{t.stageName} · {t.instance?.definition?.name||'Workflow'}</p><div className="task-actions"><button className="btn approve" onClick={()=>act(t,'APPROVE')}><Check size={14}/> Approve</button><button className="btn reject" onClick={()=>act(t,'REJECT')}>Reject</button><button className="more-action" onClick={()=>act(t,'ESCALATE')} title="Escalate"><ArrowDownRight size={15}/></button></div></div>)}
              {pending.length===0&&<Empty text="You’re all caught up. No pending tasks."/>}
              {pending.length>4&&<button className="view-tasks" onClick={()=>setTab('My tasks')}>View all {pending.length} tasks <ArrowRight size={14}/></button>}
            </section>
          </div>
          <section className="panel templates-panel"><div className="panel-head"><div><h2>Workflow templates</h2><p>Reusable processes, configured for your team</p></div><button className="btn secondary compact" onClick={()=>setShowCreate(true)}><Plus size={15}/> Create template</button></div>
            <div className="template-grid">{definitions.slice(0,3).map((d,idx)=><div className="template-card" key={d.id}><div className={`template-icon ti-${idx%3}`}><Workflow size={19}/></div><h3>{d.name}</h3><p>{d.description||'Configurable workflow template'}</p><div className="template-meta"><span><GitBranch size={13}/> {d.stages?.length||0} stages</span><button onClick={()=>{setShowStart(true)}}>Launch <ArrowRight size={13}/></button></div></div>)}{definitions.length===0&&<Empty text="No templates created yet. Create a workflow to get started."/>}</div>
          </section>
        </>}
        {tab==='My tasks'&&<><PageTitle title="My tasks" subtitle="Review, route, and resolve workflow tasks." action={<button className="btn secondary" onClick={load}><RefreshCw size={15}/> Refresh</button>}/><section className="panel"><div className="panel-head"><div><h2>Task inbox</h2><p>Tasks are routed by the role configured on each stage.</p></div><span className="count-pill">{pending.length} pending</span></div>{pending.map(t=><div className="inbox-row" key={t.id}><div className="task-square"><FileCheck2 size={18}/></div><div className="inbox-main"><b>{t.instance?.title||`Task #${t.id}`}</b><p>{t.stageName} · {t.instance?.definition?.name} · Role: {t.requiredRole}</p><small>Created {date(t.createdAt)}</small></div><div className="inbox-actions"><button className="btn approve" onClick={()=>act(t,'APPROVE')}><Check size={14}/> Approve</button><button className="btn reject" onClick={()=>act(t,'REJECT')}>Reject</button><button className="btn secondary compact" onClick={()=>act(t,'RETURN')}>Return</button><button className="btn secondary compact" onClick={()=>act(t,'ESCALATE')}>Escalate</button></div></div>)}{pending.length===0&&<Empty text="No pending tasks right now."/>}</section></>}
        {tab==='Workflows'&&<><PageTitle title="Workflow builder" subtitle="Create reusable processes from stages, roles, and routing rules." action={<button className="btn primary" onClick={()=>setShowCreate(true)}><Plus size={16}/> New workflow</button>}/><div className="definition-grid">{definitions.map(d=><div className="panel definition-card" key={d.id}><div className="template-icon ti-0"><Workflow size={20}/></div><h2>{d.name}</h2><p>{d.description}</p><div className="stage-stack">{d.stages?.map((s,idx)=><div className="stage-line" key={s.id||s.name}><span>{idx+1}</span><div><b>{s.name}</b><small>Required role: {s.requiredRole}</small></div>{idx<d.stages.length-1&&<ArrowDownRight size={14}/>}</div>)}</div><button className="btn primary full" onClick={()=>setShowStart(true)}>Start workflow <ArrowRight size={15}/></button></div>)}{definitions.length===0&&<section className="panel"><Empty text="Create your first configurable workflow definition."/><button className="btn primary" onClick={()=>setShowCreate(true)}>Create workflow</button></section>}</div></>}
        {tab==='Executions'&&<><PageTitle title="Executions" subtitle="Inspect active, completed, and rejected workflow instances." action={<button className="btn primary" onClick={()=>setShowStart(true)}><Plus size={16}/> Start execution</button>}/><section className="panel"><div className="table-head"><span>WORKFLOW / EXECUTION</span><span>STAGE</span><span>STATUS</span><span>STARTED</span><span></span></div>{visibleInstances.map(i=><button className="table-row" key={i.id} onClick={()=>openInstance(i)}><div><b>{i.title}</b><small>{i.definition?.name} · Execution #{i.id}</small></div><span>{i.currentStage}</span><Status value={i.status}/><span>{date(i.createdAt)}</span><ArrowRight size={15}/></button>)}{visibleInstances.length===0&&<Empty text="No workflow executions found."/>}</section></>}
        {tab==='Timeline'&&<><PageTitle title="Execution timeline" subtitle={selected?`${selected.title} · Execution #${selected.id}`:'Select an execution to inspect its audit trail.'} action={<button className="btn secondary" onClick={()=>selected&&openInstance(selected)}><RefreshCw size={15}/> Refresh timeline</button>}/>{selected?<section className="panel timeline-panel"><div className="timeline-summary"><div className="flow-icon fi-0"><Workflow size={20}/></div><div><h2>{selected.title}</h2><p>{selected.definition?.name} · Current stage: {selected.currentStage}</p></div><Status value={selected.status}/></div><div className="timeline">{timeline.map((a,idx)=><div className="timeline-event" key={a.id}><div className="timeline-rail"><div className={`timeline-dot ${idx===timeline.length-1?'latest':''}`} />{idx<timeline.length-1&&<div className="timeline-line"/>}</div><div className="timeline-event-content"><div className="timeline-event-head"><b>{a.eventType.replaceAll('_',' ')}</b><time>{date(a.occurredAt)}</time></div><p>{a.details}</p><small>Actor: {a.actor}</small></div></div>)}{timeline.length===0&&<Empty text="No audit events available."/>}</div></section>:<section className="panel"><Empty text="Choose an execution from the Executions tab to view its timeline."/><button className="btn secondary" onClick={()=>setTab('Executions')}>Browse executions</button></section>}</>}
        <footer className="footer"><span>© 2026 FlowForge, Inc.</span><span><span className="green-dot"/> API connected · Demo workspace</span><span>Built for reliable work, not busywork.</span></footer>
      </div>
    </main>
    {showCreate&&<Modal title="Create workflow template" subtitle="Configure the starting approval path." onClose={()=>setShowCreate(false)}><label>Workflow name<input value={newName} onChange={e=>setNewName(e.target.value)} /></label><label>Description<textarea value={newDesc} onChange={e=>setNewDesc(e.target.value)} /></label><div className="builder-preview"><div className="builder-stage"><span>01</span><div><b>Manager review</b><small>MANAGER · condition: amount &gt; 1000</small></div></div><div className="builder-connector"/><div className="builder-stage"><span>02</span><div><b>Finance review</b><small>FINANCE · final approval</small></div></div><p>Starter rule: expenses over 1,000 route to Finance review.</p></div><div className="modal-actions"><button className="btn secondary" onClick={()=>setShowCreate(false)}>Cancel</button><button className="btn primary" disabled={busy||!newName.trim()} onClick={createDefinition}>{busy?'Creating…':'Create template'} <ArrowRight size={15}/></button></div></Modal>}
    {showStart&&<Modal title="Start a workflow" subtitle="Create an execution from a reusable definition." onClose={()=>setShowStart(false)}><label>Workflow template<select defaultValue="" onChange={()=>{}}>{definitions.map(d=><option key={d.id} value={d.id}>{d.name}</option>)}</select></label><label>Execution title<input value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. Laptop purchase"/></label><div className="form-grid"><label>Amount<input type="number" value={amount} onChange={e=>setAmount(e.target.value)}/></label><label>Department<input value={department} onChange={e=>setDepartment(e.target.value)}/></label></div><div className="notice"><Zap size={15}/> Context fields can be used by conditional routing rules.</div><div className="modal-actions"><button className="btn secondary" onClick={()=>setShowStart(false)}>Cancel</button><button className="btn primary" disabled={busy||definitions.length===0||!title.trim()} onClick={startInstance}>{busy?'Starting…':'Start execution'} <ArrowRight size={15}/></button></div></Modal>}
  </div>
}

function Stat({icon,label,value,trend,tone}:{icon:React.ReactNode;label:string;value:string;trend:string;tone:string}){return <div className="stat-card"><div className={`stat-icon ${tone}`}>{icon}</div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-trend">{trend}</div></div>}
function Status({value}:{value:string}){return <span className={`status status-${value?.toLowerCase()}`}>{value==='RUNNING'?'● ':value==='APPROVED'?'✓ ':''}{shortStatus(value)}</span>}
function Empty({text}:{text:string}){return <div className="empty-state"><div className="empty-icon"><Workflow size={20}/></div><p>{text}</p></div>}
function PageTitle({title,subtitle,action}:{title:string;subtitle:string;action?:React.ReactNode}){return <div className="page-title"><div><div className="eyebrow">WORKFLOW OPERATIONS</div><h1>{title}</h1><p className="subtitle">{subtitle}</p></div>{action}</div>}
function Modal({title,subtitle,onClose,children}:{title:string;subtitle:string;onClose:()=>void;children:React.ReactNode}){return <div className="modal-backdrop" onMouseDown={e=>{if(e.target===e.currentTarget)onClose()}}><div className="modal"><div className="modal-head"><div><h2>{title}</h2><p>{subtitle}</p></div><button className="icon-button" onClick={onClose}><X size={17}/></button></div>{children}</div></div>}
