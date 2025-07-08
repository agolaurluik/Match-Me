import { Navigate, Outlet, useLocation } from 'react-router-dom';

const ProtectedRoute = () => {
    const token = localStorage.getItem('Authorization');
    const location = useLocation();

    const rawToken = token?.startsWith('Bearer ') ? token.slice(7) : token;

    const isAuthenticated = !!rawToken && rawToken !== 'null' && rawToken !== 'undefined';

    return isAuthenticated ? (
        <Outlet />
    ) : (
        <Navigate to="/" state={{ from: location }} replace />
    );
};

export default ProtectedRoute;