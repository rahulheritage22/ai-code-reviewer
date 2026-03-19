import { useEffect, useState } from 'react';
import { getRepositories, toggleRepositoryReview, addRepository } from '../api/client';
import { Activity, Github, LogOut, CheckCircle2, Circle, Plus, X } from 'lucide-react';

export default function Dashboard() {
  const [repos, setRepos] = useState<any[]>([]);
  const [isAdding, setIsAdding] = useState(false);
  const [newRepoForm, setNewRepoForm] = useState({ fullName: '', webhookSecret: '' });

  const loadRepos = () => getRepositories().then(setRepos).catch(console.error);

  useEffect(() => {
    loadRepos();
  }, []);

  const handleToggle = async (id: number) => {
    // Optimistic UI toggle for snappy feel
    setRepos(repos.map(r => r.id === id ? { ...r, reviewEnabled: !r.reviewEnabled } : r));
    try {
      await toggleRepositoryReview(id);
    } catch {
      // Revert quietly on backend block
      loadRepos();
    }
  };

  const handleAddRepo = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await addRepository(newRepoForm.fullName, newRepoForm.webhookSecret);
      setIsAdding(false);
      setNewRepoForm({ fullName: '', webhookSecret: '' });
      loadRepos();
    } catch (error) {
      alert("Failed to add repository: Make sure it's valid and not already monitored.");
    }
  };

  return (
    <div className="min-h-screen bg-[#0A0A0A] text-white p-4 md:p-8 relative">
      <div className="max-w-6xl mx-auto z-10 relative">
        <header className="flex flex-col md:flex-row justify-between items-center mb-12 backdrop-blur-md bg-white/5 p-4 md:p-6 rounded-3xl border border-white/10 shadow-[0_8px_30px_rgb(0,0,0,0.4)]">
          <div className="flex items-center gap-4 mb-4 md:mb-0">
            <div className="bg-indigo-500/20 p-2 rounded-xl ring-1 ring-indigo-500/40 shadow-[0_0_30px_rgba(99,102,241,0.2)]">
              <Activity className="w-8 h-8 text-indigo-400" />
            </div>
            <h1 className="text-2xl font-bold tracking-tight bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">Review Dashboard</h1>
          </div>
          <button 
            onClick={() => window.location.href = 'http://localhost:8080/api/logout'}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 transition-colors border border-white/10 text-gray-300 hover:text-white shadow-lg"
          >
            <LogOut className="w-4 h-4" /> Sign Out
          </button>
        </header>

        <div className="mb-8 relative z-10">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-xl font-semibold flex items-center gap-2">
              <Github className="w-6 h-6 text-gray-400" /> Monitored Repositories
            </h2>
            <button 
              onClick={() => setIsAdding(true)}
              className="group flex items-center gap-2 px-4 py-2 rounded-xl bg-indigo-500/10 hover:bg-indigo-500/20 transition-all duration-300 border border-indigo-500/30 text-indigo-300 hover:text-indigo-200 hover:shadow-[0_0_20px_rgba(99,102,241,0.2)]"
            >
              <Plus className="w-4 h-4 group-hover:rotate-90 transition-transform duration-300" /> Add Repository
            </button>
          </div>

          {repos.length === 0 ? (
            <div className="text-center p-16 backdrop-blur-xl bg-white/[0.02] border border-white/5 rounded-3xl text-gray-400 shadow-2xl">
               <Activity className="w-12 h-12 mx-auto mb-4 text-gray-600 opacity-50" />
               <p className="text-lg">No repositories are being monitored yet.</p>
               <p className="text-sm text-gray-500 mt-2">Connect one to start autonomous AI reviews!</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {repos.map(repo => (
                <div 
                  key={repo.id} 
                  className="group relative bg-white/[0.02] border border-white/10 p-6 rounded-3xl hover:bg-white/[0.04] transition-all duration-300 hover:shadow-[0_8px_30px_rgba(99,102,241,0.05)] hover:-translate-y-1 hover:border-white/20 overflow-hidden cursor-pointer"
                  onClick={() => handleToggle(repo.id)}
                >
                  <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-500/5 rounded-bl-full -z-10 group-hover:scale-125 transition-transform duration-500" />
                  
                  <h3 className="text-lg font-medium mb-4 truncate pr-8">{repo.fullName}</h3>
                  <div className="flex items-center justify-between mt-auto">
                    <span className={`text-sm font-medium px-3 py-1.5 rounded-full flex items-center gap-1.5 transition-colors ${repo.reviewEnabled ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 shadow-[0_0_15px_rgba(99,102,241,0.2)]' : 'bg-white/5 text-gray-400 border border-white/5'}`}>
                      {repo.reviewEnabled ? <CheckCircle2 className="w-4 h-4" /> : <Circle className="w-4 h-4" />}
                      {repo.reviewEnabled ? 'AI Active' : 'Enable AI Review'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      
      {/* Absolute Ambient Background */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden mix-blend-screen">
         <div className="absolute top-[20%] right-[-10%] w-[600px] h-[600px] bg-indigo-500/10 blur-[150px] rounded-full animate-pulse" />
         <div className="absolute bottom-[-10%] left-[-10%] w-[800px] h-[800px] bg-violet-600/10 blur-[150px] rounded-full pointer-events-none" />
      </div>

      {/* Add Repository Blur Modal */}
      {isAdding && (
        <div className="fixed inset-0 z-50 flex flex-col justify-center items-center bg-black/60 backdrop-blur-md p-4 animate-in fade-in duration-300">
           <form onSubmit={handleAddRepo} className="w-full max-w-md bg-[#111] border border-white/10 p-8 rounded-3xl shadow-2xl relative transition-transform animate-in zoom-in-95 duration-300">
             <button type="button" onClick={() => setIsAdding(false)} className="absolute top-6 right-6 text-gray-400 hover:text-white transition-colors"><X className="w-5 h-5" /></button>
             <h3 className="text-2xl font-bold mb-6 tracking-tight">Attach New Codebase</h3>
             
             <div className="space-y-5">
                <div>
                   <label className="block text-sm font-medium text-gray-400 mb-2">GitHub Repository</label>
                   <input required value={newRepoForm.fullName} onChange={e => setNewRepoForm(f => ({...f, fullName: e.target.value}))} placeholder="e.g. facebook/react" className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all font-mono text-sm placeholder:text-gray-600" />
                </div>
                <div>
                   <label className="block text-sm font-medium text-gray-400 mb-2">GitHub Webhook Secret</label>
                   <input required type="password" value={newRepoForm.webhookSecret} onChange={e => setNewRepoForm(f => ({...f, webhookSecret: e.target.value}))} placeholder="Super secret HMAC key" className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all font-mono text-sm placeholder:text-gray-600" />
                </div>
             </div>

             <button type="submit" className="w-full mt-8 bg-indigo-500 border border-indigo-400 shadow-[0_0_20px_rgba(99,102,241,0.3)] hover:shadow-[0_0_30px_rgba(99,102,241,0.5)] text-white font-semibold py-3.5 rounded-xl transition-all hover:bg-indigo-400 active:scale-95">
                Connect and Monitor
             </button>
           </form>
        </div>
      )}
    </div>
  );
}
