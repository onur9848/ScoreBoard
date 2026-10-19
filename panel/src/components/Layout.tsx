import { useQueryClient } from '@tanstack/react-query';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth';

export function Layout() {
  const { signOut } = useAuth();
  const queryClient = useQueryClient();
  return (
    <div className="shell">
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/" className="brand">
            <img src="/favicon.svg" alt="" width={28} height={28} />
            <span>Puan Tablosu</span>
          </NavLink>
          <nav className="nav">
            <NavLink to="/" end>
              Özet
            </NavLink>
            <NavLink to="/oyunlar">Oyunlar</NavLink>
          </nav>
          <button
            className="ghost"
            onClick={() => {
              queryClient.clear();
              signOut();
            }}
          >
            Çıkış
          </button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
