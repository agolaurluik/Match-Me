import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import './index.css';

import Frontpage from './Pages/Frontpage.jsx';
import BioPage from './Pages/BioPage.jsx';
import NotFound from './Pages/NotFound.jsx';
import ConnectionsPage from './Pages/ConnectionsPage.jsx';
import ProfilePage from './Pages/ProfilePage.jsx';

import ProtectedRoute from './components/SecurityComponents/ProtectedRoute.jsx';
import PageLayout from './Pages/PageLayout.jsx';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>

                <Route element={<PageLayout />}>

                    <Route path="/" element={<Frontpage />} />

                    <Route element={<ProtectedRoute />}>
                        <Route path="/bio" element={<BioPage />} />
                        <Route path="/profile" element={<ProfilePage />} />
                        <Route path="/connections" element={<ConnectionsPage />} />
                    </Route>

                </Route>

                <Route path="*" element={<NotFound />} />

            </Routes>
        </BrowserRouter>
    </StrictMode>,
);