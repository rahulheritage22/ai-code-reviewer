
import { Github, Zap } from 'lucide-react';

export default function Login() {
  const handleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  };

  return (
    <div className="min-h-screen flex relative overflow-hidden bg-[#0A0A0A]">
      {/* Dynamic Background Gradients */}
      <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-indigo-600/20 blur-[120px] rounded-full pointer-events-none animate-pulse" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-violet-600/20 blur-[120px] rounded-full pointer-events-none" />
      
      <div className="flex-1 flex flex-col justify-center items-center p-8 z-10 w-full">
        <div className="w-full max-w-md backdrop-blur-xl bg-white/[0.03] border border-white/10 p-10 rounded-3xl shadow-2xl transition-all hover:shadow-indigo-500/10 hover:border-white/20 duration-500">
          <div className="flex items-center justify-center mb-8">
            <div className="bg-indigo-500/10 p-4 rounded-2xl ring-1 ring-indigo-500/30 shadow-[0_0_30px_rgba(99,102,241,0.2)]">
              <Zap className="w-10 h-10 text-indigo-400" />
            </div>
          </div>
          
          <h1 className="text-4xl font-black text-center mb-3 tracking-tight bg-gradient-to-r from-indigo-300 via-white to-violet-300 bg-clip-text text-transparent">
            AI Code Reviewer
          </h1>
          <p className="text-gray-400 text-center mb-10 text-lg">
            Your autonomous senior engineer.
          </p>
          
          <button 
            onClick={handleLogin}
            className="w-full relative group bg-white text-black font-semibold py-4 px-6 rounded-2xl flex items-center justify-center gap-3 overflow-hidden transition-all duration-300 hover:scale-[1.02] shadow-[0_0_20px_rgba(255,255,255,0.1)] hover:shadow-[0_0_40px_rgba(255,255,255,0.3)]"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-gray-200 to-white opacity-0 group-hover:opacity-100 transition-opacity" />
            <Github className="w-6 h-6 z-10" />
            <span className="z-10 text-lg">Continue with GitHub</span>
          </button>
        </div>
      </div>
    </div>
  );
}
