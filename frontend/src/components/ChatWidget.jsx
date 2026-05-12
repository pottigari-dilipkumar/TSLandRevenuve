// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, Trash2 } from 'lucide-react';
import { sendChatMessage, sendPublicChatMessage, getChatHistory, clearChatHistory } from '../api/chatApi';
import { useAuthStore } from '../store/authStore';

const AUTH_QUICK_ACTIONS = [
  { label: 'Check Status',       message: 'How do I check my registration status?' },
  { label: 'Documents Required', message: 'What documents are required for land registration?' },
  { label: 'Fees & Stamp Duty',  message: 'What are the registration fees and stamp duty?' },
  { label: 'Office Hours',       message: 'What are the Sub-Registrar office hours and contact?' },
];

const PUBLIC_QUICK_ACTIONS = [
  { label: 'Login Issues',       message: "I'm having trouble logging in, can you help?" },
  { label: 'OTP Not Received',   message: "I didn't receive my OTP, what should I do?" },
  { label: 'Citizen Login',      message: 'How do I login as a citizen using Aadhaar?' },
  { label: 'Contact Office',     message: 'What are the Sub-Registrar office hours and contact?' },
];

export default function ChatWidget() {
  const { token } = useAuthStore();
  const isAuthenticated = Boolean(token);

  const [isOpen, setIsOpen]               = useState(false);
  const [messages, setMessages]           = useState([]);
  const [input, setInput]                 = useState('');
  const [loading, setLoading]             = useState(false);
  const [historyLoaded, setHistoryLoaded] = useState(false);
  const messagesEndRef                    = useRef(null);
  const inputRef                          = useRef(null);

  useEffect(() => {
    if (isOpen && !historyLoaded) {
      if (isAuthenticated) {
        getChatHistory()
          .then(history => { setMessages(history || []); setHistoryLoaded(true); })
          .catch(() => setHistoryLoaded(true));
      } else {
        setHistoryLoaded(true);
      }
    }
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen, historyLoaded, isAuthenticated]);

  // Reset history state when auth status changes (login / logout)
  useEffect(() => {
    setMessages([]);
    setHistoryLoaded(false);
  }, [isAuthenticated]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const sendMessage = async (text) => {
    const trimmed = text.trim();
    if (!trimmed || loading) return;
    setMessages(prev => [...prev, {
      sender: 'USER', message: trimmed, createdAt: new Date().toISOString(),
    }]);
    setInput('');
    setLoading(true);
    try {
      const res = isAuthenticated
        ? await sendChatMessage(trimmed)
        : await sendPublicChatMessage(trimmed);
      setMessages(prev => [...prev, {
        sender: 'BOT',
        message: res.botMessage || res.message,
        createdAt: new Date().toISOString(),
      }]);
    } catch {
      setMessages(prev => [...prev, {
        sender: 'BOT',
        message: "I'm having trouble connecting right now. Please try again in a moment.",
        createdAt: new Date().toISOString(),
      }]);
    } finally {
      setLoading(false);
    }
  };

  const handleClear = async () => {
    if (isAuthenticated) await clearChatHistory().catch(() => {});
    setMessages([]);
    setHistoryLoaded(true);
  };

  const quickActions = isAuthenticated ? AUTH_QUICK_ACTIONS : PUBLIC_QUICK_ACTIONS;
  const showQuickActions = messages.length === 0 && !loading;

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">

      {/* Chat panel */}
      {isOpen && (
        <div className="w-[360px] rounded-2xl bg-white border border-slate-200 overflow-hidden animate-fade-in"
             style={{ boxShadow: '0 20px 60px rgba(15,23,42,0.15), 0 4px 16px rgba(99,102,241,0.08)' }}>

          {/* Header */}
          <div className="bg-dark-gradient px-4 py-3 flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-brand-gradient flex items-center justify-center shadow-glow-sm flex-shrink-0">
              <MessageSquare size={16} className="text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white leading-none">LRMS Support</p>
              <div className="flex items-center gap-1.5 mt-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse flex-shrink-0" />
                <span className="text-[10px] text-slate-400">Online — replies instantly</span>
              </div>
            </div>
            {messages.length > 0 && (
              <button
                onClick={handleClear}
                title="Clear chat"
                className="text-slate-500 hover:text-red-400 transition-colors p-1 flex-shrink-0"
              >
                <Trash2 size={14} />
              </button>
            )}
            <button
              onClick={() => setIsOpen(false)}
              className="text-slate-400 hover:text-white transition-colors flex-shrink-0"
            >
              <X size={16} />
            </button>
          </div>

          {/* Messages area */}
          <div className="h-72 overflow-y-auto p-4 bg-slate-50 space-y-3">
            {messages.length === 0 && !loading && (
              <div className="flex flex-col items-center justify-center h-full text-center pb-2">
                <div className="w-12 h-12 rounded-2xl bg-brand-50 flex items-center justify-center mb-3">
                  <MessageSquare size={20} className="text-brand-500" />
                </div>
                <p className="text-sm font-semibold text-slate-700">How can I help you?</p>
                <p className="text-xs text-slate-400 mt-1 max-w-[220px] leading-relaxed">
                  {isAuthenticated
                    ? 'Ask about registration, documents, fees, mutations, or office hours.'
                    : 'Ask about login, OTP issues, citizen authentication, or office contacts.'}
                </p>
              </div>
            )}

            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.sender === 'USER' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[82%] px-3.5 py-2.5 rounded-2xl text-sm leading-relaxed whitespace-pre-line ${
                  msg.sender === 'USER'
                    ? 'bg-brand-gradient text-white rounded-br-md'
                    : 'bg-white text-slate-700 shadow-sm border border-slate-100 rounded-bl-md'
                }`}>
                  {msg.message}
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-white shadow-sm border border-slate-100 rounded-2xl rounded-bl-md px-4 py-3">
                  <div className="flex gap-1 items-center h-4">
                    <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce"
                          style={{ animationDelay: '0ms' }} />
                    <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce"
                          style={{ animationDelay: '150ms' }} />
                    <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce"
                          style={{ animationDelay: '300ms' }} />
                  </div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick action chips */}
          {showQuickActions && (
            <div className="px-4 py-2.5 bg-slate-50 border-t border-slate-100 flex flex-wrap gap-1.5">
              {quickActions.map(({ label, message }) => (
                <button
                  key={label}
                  onClick={() => sendMessage(message)}
                  className="text-xs bg-white text-brand-600 border border-brand-100 rounded-full px-3 py-1.5 hover:bg-brand-50 hover:border-brand-300 transition-colors font-medium shadow-sm"
                >
                  {label}
                </button>
              ))}
            </div>
          )}

          {/* Input row */}
          <div className="border-t border-slate-100 p-3 flex items-center gap-2 bg-white">
            <input
              ref={inputRef}
              type="text"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(input); }
              }}
              placeholder="Type your question…"
              className="input text-sm py-2"
              maxLength={1000}
            />
            <button
              onClick={() => sendMessage(input)}
              disabled={!input.trim() || loading}
              className="w-9 h-9 rounded-xl bg-brand-gradient flex items-center justify-center shadow-sm disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90 transition-opacity flex-shrink-0"
            >
              <Send size={14} className="text-white" />
            </button>
          </div>
        </div>
      )}

      {/* Toggle button */}
      <button
        onClick={() => setIsOpen(v => !v)}
        title="LRMS Support Chat"
        className="w-14 h-14 rounded-2xl bg-brand-gradient shadow-lg hover:shadow-xl hover:scale-105 transition-all duration-300 flex items-center justify-center relative"
      >
        {isOpen ? (
          <X size={22} className="text-white" />
        ) : (
          <>
            <MessageSquare size={22} className="text-white" />
            <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-emerald-400 rounded-full border-2 border-white" />
          </>
        )}
      </button>
    </div>
  );
}
